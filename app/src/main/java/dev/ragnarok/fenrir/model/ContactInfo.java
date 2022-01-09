package dev.ragnarok.fenrir.model;


public class ContactInfo {

    private final int userId;

    private String descriprion;

    private String phone;

    private String email;

    public ContactInfo(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public ContactInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getDescriprion() {
        return descriprion;
    }

    public ContactInfo setDescriprion(String descriprion) {
        this.descriprion = descriprion;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public ContactInfo setPhone(String phone) {
        this.phone = phone;
        return this;
    }
}