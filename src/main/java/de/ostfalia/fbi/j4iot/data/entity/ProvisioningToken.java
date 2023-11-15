package de.ostfalia.fbi.j4iot.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class ProvisioningToken extends AbstractEntity {
    @NotNull Boolean isActive = true;
    @NotEmpty @Column(length = 80) String token; // TODO unique+index
    @NotNull Integer authTokenExpiresInMinutes = 7*24*60;
    // TODO expires_at: Mapped[datetime] = mapped_column(DateTime, index=True)
    // TODO created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    // TODO updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    // TODO last_use_at: Mapped[Optional[datetime]] = mapped_column(DateTime, default=None)

    @ManyToOne
    @JsonIgnoreProperties({"tags", "provisioningTokens", "devices"})
    @NotNull Project project;
}
