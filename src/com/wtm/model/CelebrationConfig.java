package com.wtm.model;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Local team-recognition record.
 *
 * A single employee record can independently participate in birthday,
 * work-anniversary, and Employee of the Month recognition.
 *
 * Birthday year is intentionally not stored; only month/day are needed.
 * Hire date retains its year so work-anniversary years can be calculated.
 * Employee of the Month stores the selected month/year so recognition expires
 * automatically when a new month begins.
 */
public record CelebrationConfig(
        String name,
        int birthdayMonth,
        int birthdayDay,
        LocalDate hireDate,
        String photoPath,
        boolean showBirthday,
        boolean showAnniversary,
        int employeeOfMonthYear,
        int employeeOfMonthMonth,
        boolean celebrationEffect,
        boolean enabled
) {
    public boolean birthdayToday(LocalDate today){
        return enabled && showBirthday
                && birthdayMonth==today.getMonthValue()
                && birthdayDay==today.getDayOfMonth();
    }

    public boolean anniversaryToday(LocalDate today){
        return enabled && showAnniversary && hireDate!=null
                && hireDate.getMonthValue()==today.getMonthValue()
                && hireDate.getDayOfMonth()==today.getDayOfMonth()
                && !hireDate.isAfter(today);
    }

    public int anniversaryYears(LocalDate today){
        return hireDate==null?0:Math.max(0,today.getYear()-hireDate.getYear());
    }

    public boolean employeeOfMonth(YearMonth month){
        return enabled
                && month!=null
                && employeeOfMonthYear==month.getYear()
                && employeeOfMonthMonth==month.getMonthValue();
    }

    public boolean employeeOfMonthToday(LocalDate today){
        return today!=null && employeeOfMonth(YearMonth.from(today));
    }
}
