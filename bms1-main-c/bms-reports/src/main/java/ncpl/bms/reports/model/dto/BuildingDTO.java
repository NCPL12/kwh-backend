package ncpl.bms.reports.model.dto;

public class BuildingDTO {
    private Integer id;
    private String buildingName;
    private String address;
    private Double totalArea;
    private String email;
    private String personOfContact;
    private String phoneNumber;
    private String accountName;
    private String accountNumber;
    private String ifscCode;
    private Boolean schedule;
    private Boolean sendMail;

    // Default Constructor
    public BuildingDTO() {}

    // Parameterized Constructor
    public BuildingDTO(Integer id, String buildingName, String address, Double totalArea, String email, String personOfContact, String phoneNumber, String accountName, String accountNumber, String ifscCode, Boolean schedule, Boolean sendMail) {
        this.id = id;
        this.buildingName = buildingName;
        this.address = address;
        this.totalArea = totalArea;
        this.email = email;
        this.personOfContact = personOfContact;
        this.phoneNumber = phoneNumber;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.schedule = schedule;
        this.sendMail = sendMail;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(Double totalArea) {
        this.totalArea = totalArea;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPersonOfContact() {
        return personOfContact;
    }

    public void setPersonOfContact(String personOfContact) {
        this.personOfContact = personOfContact;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public Boolean getSchedule() {
        return schedule;
    }

    public void setSchedule(Boolean schedule) {
        this.schedule = schedule;
    }

    public Boolean getSendMail() {
        return sendMail;
    }

    public void setSendMail(Boolean sendMail) {
        this.sendMail = sendMail;
    }
}
