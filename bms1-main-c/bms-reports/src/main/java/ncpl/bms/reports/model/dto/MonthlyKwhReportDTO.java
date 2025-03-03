package ncpl.bms.reports.model.dto;

public class MonthlyKwhReportDTO {
    private String month; // Format: YYYY-MM
    private double monthlyKwh;
    private String tableName; // Added field to store the table name

    // Constructor
    public MonthlyKwhReportDTO(String month, double monthlyKwh, String tableName) {
        this.month = month;
        this.monthlyKwh = monthlyKwh;
        this.tableName = tableName;
    }

    // Getters and Setters
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getMonthlyKwh() {
        return monthlyKwh;
    }

    public void setMonthlyKwh(double monthlyKwh) {
        this.monthlyKwh = monthlyKwh;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
