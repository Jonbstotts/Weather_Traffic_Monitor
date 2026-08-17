package com.wtm.model;

import java.time.LocalDate;

/**
 * Local team-recognition record.
 *
 * Birthday year is intentionally not stored; only month/day are needed.
 * Hire date retains its year so work-anniversary years can be calculated.
 */
public record CelebrationConfig(
        String name,
        int birthdayMonth,
        int birthdayDay,
        LocalDate hireDate,
        String photoPath,
        boolean showBirthday,
        boolean showAnniversary,
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
}
