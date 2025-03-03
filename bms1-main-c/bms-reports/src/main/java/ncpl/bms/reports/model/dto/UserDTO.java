package ncpl.bms.reports.model.dto;

public class UserDTO {
    private Integer id;
    private String username;
    private String role;
    private String password;
    private String fullName;
    private String phoneNumber;

    private Boolean isDeleted;

    // Default constructor
    public UserDTO() {}

    // Parameterized constructor
    public UserDTO(Integer id, String username, String role, String password, String fullName, String phoneNumber, Boolean isDeleted) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.isDeleted= isDeleted;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
