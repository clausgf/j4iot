package de.ostfalia.fbi.j4iot.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user_",
    indexes = {
        @Index(columnList = "name", unique = true)
})
public class User extends AbstractEntity{
    @NotEmpty String name; // TODO index unique
    String passwordSalt;
    String passwordHash;
    @NotNull String firstName = "";
    @NotNull String lastName = "";
    @Email @NotEmpty String email = "";
    @NotNull Boolean isActive = true;
//    TODO hashed_password: Mapped[str] = mapped_column(String(255))
//    TODO created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
//    TODO updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
//    TODO last_login_at: Mapped[Optional[datetime]] = mapped_column(DateTime, default=None)
//    TODO groups: Mapped[List["Usergroup"]] = relationship(secondary=user_usergroups, back_populates="users")
    //  TODO Besser mit Rollen arbeiten?!!!
    // TODO Wie kann man Rollen für einzelne Projekte vergeben?
    // TODO isDeleted???

    @Override
    public String toString() {
        return name;
    }

    public java.lang.String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

//
//class Usergroup(Base):
//        __tablename__ = "usergroups"
//        id: Mapped[int] = mapped_column(primary_key=True, index=True)
//        groupname: Mapped[str] = mapped_column(String(40), unique=True, index=True)
//        description: Mapped[str] = mapped_column(String(200), default="")
//        is_active: Mapped[bool] = mapped_column(Boolean, default=True)
//        created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
//        updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
//
//        users: Mapped[List["User"]] = relationship(secondary=user_usergroups, back_populates="groups")
