import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import worker, { type Env } from '../src/index';

const env: Env = {
	WORKER_SHARED_SECRET: 'test-secret',
	RESEND_API_KEY: 'resend-key',
	NOTIFY_FROM_EMAIL: 'notifications@kammo.co.za',
};

const validBody = {
	dealCode: 'DEAL0001',
	sellerEmail: 'seller@example.com',
	sellerName: 'KM0002',
	itemName: 'Widget',
	price: 100,
	dealId: 10,
	inspectionWindowHours: 72,
};

function postRequest(body: unknown, headers: Record<string, string> = {}): Request {
	return new Request('https://worker.example.com/notify/payment-confirmed', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json', ...headers },
		body: JSON.stringify(body),
	});
}

describe('payment-notify worker', () => {
	beforeEach(() => {
		vi.stubGlobal(
			'fetch',
			vi.fn(async () => new Response('{}', { status: 200 }))
		);
	});

	afterEach(() => {
		vi.unstubAllGlobals();
	});

	it('rejects requests missing the shared secret', async () => {
		const response = await worker.fetch(postRequest(validBody), env);
		expect(response.status).toBe(401);
		expect(fetch).not.toHaveBeenCalled();
	});

	it('rejects requests with the wrong shared secret', async () => {
		const response = await worker.fetch(
			postRequest(validBody, { 'X-Kammo-Worker-Secret': 'wrong' }),
			env
		);
		expect(response.status).toBe(401);
	});

	it('rejects non-POST or unknown routes', async () => {
		const response = await worker.fetch(
			new Request('https://worker.example.com/notify/payment-confirmed', { method: 'GET' }),
			env
		);
		expect(response.status).toBe(404);
	});

	it('rejects payloads missing required fields', async () => {
		const response = await worker.fetch(
			postRequest({ dealCode: 'DEAL0001' }, { 'X-Kammo-Worker-Secret': 'test-secret' }),
			env
		);
		expect(response.status).toBe(400);
	});

	it('sends the email and returns 202 for a valid authenticated request', async () => {
		const response = await worker.fetch(
			postRequest(validBody, { 'X-Kammo-Worker-Secret': 'test-secret' }),
			env
		);

		expect(response.status).toBe(202);
		expect(fetch).toHaveBeenCalledWith(
			'https://api.resend.com/emails',
			expect.objectContaining({ method: 'POST' })
		);
		const [, init] = (fetch as ReturnType<typeof vi.fn>).mock.calls[0];
		const sentBody = JSON.parse(init.body as string);
		expect(sentBody.to).toBe('seller@example.com');
		expect(sentBody.subject).toContain('DEAL0001');
		expect(sentBody.text).toContain('72-hour');
	});

	it('returns 502 when the email provider call fails', async () => {
		vi.stubGlobal(
			'fetch',
			vi.fn(async () => new Response('boom', { status: 500 }))
		);

		const response = await worker.fetch(
			postRequest(validBody, { 'X-Kammo-Worker-Secret': 'test-secret' }),
			env
		);

		expect(response.status).toBe(502);
	});
});
