package ncpl.bms.reports.model.dto;

public class DailyKwhReportDTO {
    private String date;
    private Double dailyKwh;
    private String tableName;

    public DailyKwhReportDTO(String date, Double dailyKwh, String tableName) {
        this.date = date;
        this.dailyKwh = dailyKwh;
        this.tableName = tableName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getDailyKwh() {
        return dailyKwh;
    }

    public void setDailyKwh(Double dailyKwh) {
        this.dailyKwh = dailyKwh;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


}
