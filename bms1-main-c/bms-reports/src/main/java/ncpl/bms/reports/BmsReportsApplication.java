package ncpl.bms.reports;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "BMS Reporting Application",
				description = "This is an API server."
		)
)
@EnableScheduling // Enable scheduling
public class 	BmsReportsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmsReportsApplication.class, args);
	}
}