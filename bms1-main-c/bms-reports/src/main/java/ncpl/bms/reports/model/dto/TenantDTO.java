package ncpl.bms.reports.model.dto;

public class TenantDTO {

    private Integer id;
    private String name;
    private String address;
    private String personOfContact;
    private String mobileNumber;
    private String unitAddress;
    private Integer areaOccupied; // New field
    private String email;       // New field
    private Boolean isDeleted;

    // Default constructor
    public TenantDTO() {}

    // Parameterized constructor
    public TenantDTO(Integer id, String name, String address, String personOfContact, String mobileNumber, String unitAddress, Integer areaOccupied, String email, Boolean isDeleted) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.personOfContact = personOfContact;
        this.mobileNumber = mobileNumber;
        this.unitAddress = unitAddress;
        this.areaOccupied = areaOccupied;
        this.email = email;
        this.isDeleted= isDeleted;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPersonOfContact() {
        return personOfContact;
    }

    public void setPersonOfContact(String personOfContact) {
        this.personOfContact = personOfContact;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getUnitAddress() {
        return unitAddress;
    }

    public void setUnitAddress(String unitAddress) {
        this.unitAddress = unitAddress;
    }

    public Integer getAreaOccupied() {
        return areaOccupied;
    }

    public void setAreaOccupied(Integer areaOccupied) {
        this.areaOccupied = areaOccupied;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
