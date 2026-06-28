export interface Env {
	WORKER_SHARED_SECRET: string;
	RESEND_API_KEY: string;
	NOTIFY_FROM_EMAIL: string;
}

interface PaymentConfirmedNotifyRequest {
	dealCode: string;
	sellerEmail: string;
	sellerName: string;
	itemName: string;
	price: number;
	dealId: number;
	inspectionWindowHours?: number;
}

function isPaymentConfirmedNotifyRequest(body: unknown): body is PaymentConfirmedNotifyRequest {
	if (typeof body !== 'object' || body === null) {
		return false;
	}
	const candidate = body as Record<string, unknown>;
	return (
		typeof candidate.dealCode === 'string' &&
		typeof candidate.sellerEmail === 'string' &&
		typeof candidate.sellerName === 'string' &&
		typeof candidate.itemName === 'string' &&
		typeof candidate.price === 'number' &&
		typeof candidate.dealId === 'number'
	);
}

function renderEmail(payload: PaymentConfirmedNotifyRequest): { subject: string; text: string; html: string } {
	const windowHours = payload.inspectionWindowHours ?? 72;
	const subject = `Payment confirmed for deal ${payload.dealCode} — please dispatch ${payload.itemName}`;
	const text =
		`Hi ${payload.sellerName},\n\n` +
		`The buyer's payment for deal ${payload.dealCode} (${payload.itemName}, R${payload.price.toFixed(2)}) ` +
		`has been confirmed and secured by Kammo.\n\n` +
		`Please dispatch the item now. Once the buyer receives it, they have a ${windowHours}-hour inspection ` +
		`window before funds are released to you — the sooner you ship, the sooner that clock (and your payout) ` +
		`starts moving.\n\n` +
		`— Kammo`;
	const html =
		`<p>Hi ${payload.sellerName},</p>` +
		`<p>The buyer's payment for deal <strong>${payload.dealCode}</strong> ` +
		`(${payload.itemName}, R${payload.price.toFixed(2)}) has been confirmed and secured by Kammo.</p>` +
		`<p>Please dispatch the item now. Once the buyer receives it, they have a <strong>${windowHours}-hour</strong> ` +
		`inspection window before funds are released to you — the sooner you ship, the sooner that clock (and your ` +
		`payout) starts moving.</p>` +
		`<p>— Kammo</p>`;
	return { subject, text, html };
}

async function sendEmail(env: Env, payload: PaymentConfirmedNotifyRequest): Promise<void> {
	const { subject, text, html } = renderEmail(payload);
	const response = await fetch('https://api.resend.com/emails', {
		method: 'POST',
		headers: {
			Authorization: `Bearer ${env.RESEND_API_KEY}`,
			'Content-Type': 'application/json',
		},
		body: JSON.stringify({
			from: env.NOTIFY_FROM_EMAIL,
			to: payload.sellerEmail,
			subject,
			text,
			html,
		}),
	});

	if (!response.ok) {
		const errorBody = await response.text();
		throw new Error(`Resend send failed (${response.status}): ${errorBody}`);
	}
}

export default {
	async fetch(request: Request, env: Env): Promise<Response> {
		if (request.method !== 'POST' || new URL(request.url).pathname !== '/notify/payment-confirmed') {
			return new Response('Not found', { status: 404 });
		}

		const providedSecret = request.headers.get('X-Kammo-Worker-Secret');
		if (!providedSecret || providedSecret !== env.WORKER_SHARED_SECRET) {
			return new Response('Unauthorized', { status: 401 });
		}

		let body: unknown;
		try {
			body = await request.json();
		} catch {
			return new Response('Invalid JSON body', { status: 400 });
		}

		if (!isPaymentConfirmedNotifyRequest(body)) {
			return new Response('Missing required fields', { status: 400 });
		}

		try {
			await sendEmail(env, body);
		} catch (error) {
			console.error('payment-notify: failed to send email', error);
			return new Response('Failed to send notification email', { status: 502 });
		}

		return new Response(null, { status: 202 });
	},
};
