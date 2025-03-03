package ncpl.bms.reports.model.dto;

import java.time.LocalDateTime;

public class BillingHistoryDTO {
    private int id;
    private String bill_name;
    private LocalDateTime generated_date;
    private int tenant_id;
    private String type;
    private String periods;
    private byte[] pdf_content;
    private boolean autoGenerate;


    public BillingHistoryDTO() {}

    public BillingHistoryDTO(int id, String bill_name, LocalDateTime generated_date,
                             int tenant_id, String type, String periods) {
        this.id = id;
        this.bill_name = bill_name;
        this.generated_date = generated_date;
        this.tenant_id = tenant_id;
        this.type = type;
        this.periods = periods;
    }

    public BillingHistoryDTO(int id, String bill_name, LocalDateTime generated_date,
                             int tenant_id, String type, String periods, byte[] pdf_content) {
        this.id = id;
        this.bill_name = bill_name;
        this.generated_date = generated_date;
        this.tenant_id = tenant_id;
        this.type = type;
        this.periods = periods;
        this.pdf_content = pdf_content;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBill_name() { return bill_name; }
    public void setBill_name(String bill_name) { this.bill_name = bill_name; }

    public LocalDateTime getGenerated_date() { return generated_date; }
    public void setGenerated_date(LocalDateTime generated_date) { this.generated_date = generated_date; }

    public int getTenant_id() { return tenant_id; }
    public void setTenant_id(int tenant_id) { this.tenant_id = tenant_id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPeriods() { return periods; }
    public void setPeriods(String periods) { this.periods = periods; }

    public byte[] getPdf_content() { return pdf_content; }
    public void setPdf_content(byte[] pdf_content) { this.pdf_content = pdf_content; }



    @Override
    public String toString() {
        return "BillingHistoryDTO{" +
                "id=" + id +
                ", bill_name='" + bill_name + '\'' +
                ", generated_date=" + generated_date +
                ", tenant_id=" + tenant_id +
                ", type='" + type + '\'' +
                ", periods='" + periods + '\'' +
                '}';
    }
}
