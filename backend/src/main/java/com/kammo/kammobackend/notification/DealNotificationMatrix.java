package com.kammo.kammobackend.notification;

import com.kammo.kammobackend.deal.DealStatus;
import java.util.Map;

/**
 * Maps each {@link DealStatus} a deal can transition into onto the WhatsApp template + email
 * copy sent to the buyer and the seller. Statuses with no entry (e.g. internal-only states)
 * simply trigger no notification.
 */
public final class DealNotificationMatrix {

    private static final Map<DealStatus, DealNotificationTemplate> MATRIX = Map.of(
        DealStatus.SELLER_ACCEPTED, dealAgreed(),
        DealStatus.BUYER_ACCEPTED, dealAgreed(),
        DealStatus.PAYMENT_SECURED, paymentSecured(),
        DealStatus.IN_TRANSIT, itemInTransit(),
        DealStatus.DELIVERED, inspectionWindowStarted(),
        DealStatus.COMPLETED, payoutReleased(),
        DealStatus.CANCELLED, dealCancelled(),
        DealStatus.DISPUTED, dealDisputed(),
        DealStatus.REFUNDED, dealRefunded()
    );

    private DealNotificationMatrix() {
    }

    public static DealNotificationTemplate forStatus(DealStatus status) {
        return MATRIX.get(status);
    }

    private static DealNotificationTemplate dealAgreed() {
        return new DealNotificationTemplate(
            new DealNotificationMessage(
                "kammo_deal_agreed_buyer",
                "Your Kammo deal is agreed",
                "Your deal %s for \"%s\" has been agreed. Next step: secure payment to move forward."
            ),
            new DealNotificationMessage(
                "kammo_deal_agreed_seller",
                "Your Kammo deal is agreed",
                "Your deal %s for \"%s\" has been agreed. The buyer will now secure payment."
            )
        );
    }

    private static DealNotificationTemplate paymentSecured() {
        return new DealNotificationTemplate(
            new DealNotificationMessage(
                "kammo_payment_secured_buyer",
                "Payment secured for your Kammo deal",
                "Payment for deal %s (\"%s\") is secured and held safely by Kammo. The seller has been notified to prepare your item."
            ),
            // Seller already gets an email via the payment-confirmed Worker (PaymentConfirmedNotificationListener);
            // only send WhatsApp here to avoid a duplicate email.
            new DealNotificationMessage("kammo_payment_secured_seller", null, null)
        );
    }

    private static DealNotificationTemplate itemInTransit() {
        return new DealNotificationTemplate(
            new DealNotificationMessage(
                "kammo_item_in_transit_buyer",
                "Your Kammo item is on its way",
                "Your item for deal %s (\"%s\") is now in transit."
            ),
            new DealNotificationMessage(
                "kammo_item_in_transit_seller",
                "Item marked in transit",
                "Deal %s (\"%s\") is now in transit to the buyer."
            )
        );
    }

    private static DealNotificationTemplate inspectionWindowStarted() {
        return new DealNotificationTemplate(
            new DealNotificationMessage(
                "kammo_inspection_started_buyer",
                "Inspect your delivered item",
                "Deal %s (\"%s\") has been delivered. Your inspection window is now open - raise a dispute if there's an issue, or confirm to release payment."
            ),
            new DealNotificationMessage(
                "kammo_inspection_started_seller",
                "Buyer's inspection window has started",
                "Deal %s (\"%s\") was delivered. The buyer's inspection window is now open."
            )
        );
    }

    private static DealNotificationTemplate payoutReleased() {
        return new DealNotificationTemplate(
            new DealNotificationMessage(
                "kammo_payout_released_buyer",
                "Deal completed",
                "Deal %s (\"%s\") is complete. Thanks for using Kammo!"
            ),
            new DealNotificationMessage(
                "kammo_payout_released_seller",
                "Your Kammo payout was released",
                "Your payout for deal %s (\"%s\") has been released."
            )
        );
    }

    private static DealNotificationTemplate dealCancelled() {
        return new DealNotificationTemplate(
            new DealNotificationMessage(
                "kammo_deal_cancelled_buyer",
                "Your Kammo deal was cancelled",
                "Deal %s (\"%s\") has been cancelled."
            ),
            new DealNotificationMessage(
                "kammo_deal_cancelled_seller",
                "Your Kammo deal was cancelled",
                "Deal %s (\"%s\") has been cancelled."
            )
        );
    }

    private static DealNotificationTemplate dealDisputed() {
        return new DealNotificationTemplate(
            new DealNotificationMessage(
                "kammo_deal_disputed_buyer",
                "Your Kammo dispute was raised",
                "Deal %s (\"%s\") is now under dispute review."
            ),
            new DealNotificationMessage(
                "kammo_deal_disputed_seller",
                "A dispute was raised on your Kammo deal",
                "Deal %s (\"%s\") is now under dispute review."
            )
        );
    }

    private static DealNotificationTemplate dealRefunded() {
        return new DealNotificationTemplate(
            new DealNotificationMessage(
                "kammo_deal_refunded_buyer",
                "Your Kammo payment was refunded",
                "Your payment for deal %s (\"%s\") has been refunded."
            ),
            new DealNotificationMessage(
                "kammo_deal_refunded_seller",
                "A Kammo deal refund was issued",
                "The buyer for deal %s (\"%s\") has been refunded."
            )
        );
    }
}
