package model;
import java.time.LocalDate;
import java.time.Month;

public class HolidayUtils {
    public static boolean isHoliday() {
        LocalDate today = LocalDate.now();
        
        // Example: Christmas, New Year, or Independence Day
        if (today.getMonth() == Month.DECEMBER && today.getDayOfMonth() == 31) return true;
        if (today.getMonth() == Month.JANUARY && today.getDayOfMonth() == 14) return true;
        if (today.getMonth() == Month.JULY && today.getDayOfMonth() == 24) return true;
        
        return false;
    }
}