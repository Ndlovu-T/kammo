package com.kammo.kammobackend.deal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record DealAddressRequest(
    @Size(max = 160)
    String line1,

    @Size(max = 160)
    String line2,

    @Size(max = 80)
    String city,

    @Size(max = 80)
    String province,

    @Size(max = 12)
    String postalCode,

    Double latitude,

    Double longitude,

    @Size(max = 100)
    String contactName,

    @Size(max = 30)
    String contactPhone,

    @Email
    @Size(max = 160)
    String contactEmail,

    @Size(max = 40)
    String lockerTerminalId
) {
}
