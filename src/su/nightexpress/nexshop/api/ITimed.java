package su.nightexpress.nexshop.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

public interface ITimed {

    default boolean isAvailable() {
        return this.getCurrentTimes() != null;
    }

    @NotNull
    Set<DayOfWeek> getDays();

    @NotNull
    Set<LocalTime[]> getTimes();

    @Nullable
    default LocalTime[] getCurrentTimes() {
        LocalDateTime date = LocalDateTime.now();
        DayOfWeek day = date.getDayOfWeek();
        if (!this.getDays().contains(day)) return null;

        LocalTime timeNow = LocalTime.now();
        Optional<LocalTime[]> opt = this.getTimes().stream().filter(times -> {
            return timeNow.isAfter(times[0]) && timeNow.isBefore(times[1]);
        }).findFirst();

        return opt.orElse(null);
    }
}
