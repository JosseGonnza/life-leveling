package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.quest.weekly.WeeklyQuest;
import com.lifeleveling.domain.quest.weekly.WeeklyQuestPool;
import com.lifeleveling.domain.quest.weekly.WeeklyQuestPool.WeekRotationInfo;
import com.lifeleveling.domain.quest.weekly.WeeklyQuestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WeeklyQuestPool - Selección y Rotación de Weekly Quests")
class WeeklyQuestPoolTest {

    @Nested
    class Constants {
        @Test
        @DisplayName("ACTIVE_QUESTS_PER_WEEK = 3")
        void activeQuestsPerWeek_isThree() {
            assertEquals(3, WeeklyQuestPool.ACTIVE_QUESTS_PER_WEEK);
        }

        @Test
        @DisplayName("ALL_QUEST_TYPES contiene los 6 tipos")
        void allQuestTypes_containsAll6Types() {
            assertEquals(6, WeeklyQuestPool.ALL_QUEST_TYPES.size());
            assertTrue(WeeklyQuestPool.ALL_QUEST_TYPES.containsAll(
                    List.of(WeeklyQuestType.values())));
        }
    }

    @Nested
    class Selection {
        @Test
        @DisplayName("selectForCurrentWeek() retorna exactamente 3 quests")
        void selectForCurrentWeek_returnsThreeQuests() {
            List<WeeklyQuestType> selected = WeeklyQuestPool.selectForCurrentWeek();

            assertEquals(3, selected.size());
        }

        @Test
        @DisplayName("selectForWeek() retorna exactamente 3 quests")
        void selectForWeek_returnsThreeQuests() {
            LocalDate date = LocalDate.of(2026, 1, 28);

            List<WeeklyQuestType> selected = WeeklyQuestPool.selectForWeek(date);

            assertEquals(3, selected.size());
        }

        @Test
        @DisplayName("selectForWeek() con null lanza NullPointerException")
        void selectForWeek_withNull_throwsException() {
            assertThrows(NullPointerException.class,
                    () -> WeeklyQuestPool.selectForWeek(null));
        }

        @Test
        @DisplayName("selectForWeek() retorna tipos únicos (sin duplicados)")
        void selectForWeek_returnsUniqueTypes() {
            LocalDate date = LocalDate.of(2026, 1, 28);

            List<WeeklyQuestType> selected = WeeklyQuestPool.selectForWeek(date);
            Set<WeeklyQuestType> unique = new HashSet<>(selected);

            assertEquals(selected.size(), unique.size());
        }

        @Test
        @DisplayName("selectForWeek() retorna solo tipos válidos")
        void selectForWeek_returnsValidTypes() {
            LocalDate date = LocalDate.of(2026, 1, 28);

            List<WeeklyQuestType> selected = WeeklyQuestPool.selectForWeek(date);

            assertTrue(WeeklyQuestPool.ALL_QUEST_TYPES.containsAll(selected));
        }
    }

    @Nested
    class Determinism {
        @Test
        @DisplayName("Misma semana = misma selección")
        void sameWeek_sameSelection() {
            LocalDate monday = LocalDate.of(2026, 1, 26);
            LocalDate wednesday = LocalDate.of(2026, 1, 28);
            LocalDate sunday = LocalDate.of(2026, 2, 1);

            List<WeeklyQuestType> fromMonday = WeeklyQuestPool.selectForWeek(monday);
            List<WeeklyQuestType> fromWednesday = WeeklyQuestPool.selectForWeek(wednesday);
            List<WeeklyQuestType> fromSunday = WeeklyQuestPool.selectForWeek(sunday);

            assertEquals(fromMonday, fromWednesday);
            assertEquals(fromMonday, fromSunday);
        }

        @Test
        @DisplayName("Diferente semana = (probablemente) diferente selección")
        void differentWeek_differentSelection() {
            LocalDate week1 = LocalDate.of(2026, 1, 26);
            LocalDate week2 = LocalDate.of(2026, 2, 2);
            LocalDate week3 = LocalDate.of(2026, 2, 9);

            List<WeeklyQuestType> selection1 = WeeklyQuestPool.selectForWeek(week1);
            List<WeeklyQuestType> selection2 = WeeklyQuestPool.selectForWeek(week2);
            List<WeeklyQuestType> selection3 = WeeklyQuestPool.selectForWeek(week3);

            // Al menos una debe ser diferente (estadísticamente muy probable)
            boolean allSame = selection1.equals(selection2) && selection2.equals(selection3);
            assertFalse(allSame, "Es muy improbable que 3 semanas consecutivas tengan la misma selección");
        }

        @Test
        @DisplayName("selectWithSeed() es determinístico")
        void selectWithSeed_isDeterministic() {
            long seed = 12345L;

            List<WeeklyQuestType> first = WeeklyQuestPool.selectWithSeed(seed);
            List<WeeklyQuestType> second = WeeklyQuestPool.selectWithSeed(seed);

            assertEquals(first, second);
        }

        @Test
        @DisplayName("selectWithSeed() con seeds diferentes da resultados diferentes")
        void selectWithSeed_differentSeedsDifferentResults() {
            List<WeeklyQuestType> result1 = WeeklyQuestPool.selectWithSeed(1L);
            List<WeeklyQuestType> result2 = WeeklyQuestPool.selectWithSeed(2L);

            // Muy improbable que sean iguales
            assertNotEquals(result1, result2);
        }
    }

    @Nested
    class RandomSelection {
        @Test
        @DisplayName("selectRandom() retorna 3 quests")
        void selectRandom_returnsThreeQuests() {
            List<WeeklyQuestType> selected = WeeklyQuestPool.selectRandom();

            assertEquals(3, selected.size());
        }

        @Test
        @DisplayName("selectRandom() retorna tipos únicos")
        void selectRandom_returnsUniqueTypes() {
            List<WeeklyQuestType> selected = WeeklyQuestPool.selectRandom();
            Set<WeeklyQuestType> unique = new HashSet<>(selected);

            assertEquals(selected.size(), unique.size());
        }

        @Test
        @DisplayName("selectRandom() varía entre llamadas (no determinístico)")
        void selectRandom_variesBetweenCalls() {
            // Ejecutar múltiples veces para verificar variabilidad
            Set<List<WeeklyQuestType>> uniqueSelections = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                uniqueSelections.add(WeeklyQuestPool.selectRandom());
            }

            // Debe haber múltiples selecciones únicas
            assertTrue(uniqueSelections.size() > 1,
                    "selectRandom() debe producir variabilidad");
        }
    }

    @Nested
    class QuestCreation {
        @Test
        @DisplayName("createQuestsForCurrentWeek() crea 3 WeeklyQuests")
        void createQuestsForCurrentWeek_createsThreeQuests() {
            List<WeeklyQuest> quests = WeeklyQuestPool.createQuestsForCurrentWeek();

            assertEquals(3, quests.size());
        }

        @Test
        @DisplayName("createQuestsForWeek() crea WeeklyQuests con tipos correctos")
        void createQuestsForWeek_createsQuestsWithCorrectTypes() {
            LocalDate date = LocalDate.of(2026, 1, 28);
            List<WeeklyQuestType> expectedTypes = WeeklyQuestPool.selectForWeek(date);

            List<WeeklyQuest> quests = WeeklyQuestPool.createQuestsForWeek(date);

            List<WeeklyQuestType> actualTypes = quests.stream()
                    .map(WeeklyQuest::getType)
                    .toList();

            assertEquals(expectedTypes, actualTypes);
        }

        @Test
        @DisplayName("createQuestsForWeek() asigna la semana correcta a cada quest")
        void createQuestsForWeek_assignsCorrectWeek() {
            LocalDate thursday = LocalDate.of(2026, 1, 29);
            LocalDate expectedMonday = LocalDate.of(2026, 1, 26);

            List<WeeklyQuest> quests = WeeklyQuestPool.createQuestsForWeek(thursday);

            for (WeeklyQuest quest : quests) {
                assertEquals(expectedMonday, quest.getWeekStartDate());
            }
        }

        @Test
        @DisplayName("createQuestsForWeek() crea quests con IDs únicos")
        void createQuestsForWeek_createsQuestsWithUniqueIds() {
            List<WeeklyQuest> quests = WeeklyQuestPool.createQuestsForWeek(LocalDate.now());

            Set<java.util.UUID> ids = new HashSet<>();
            for (WeeklyQuest quest : quests) {
                ids.add(quest.id().value());
            }

            assertEquals(3, ids.size());
        }
    }

    @Nested
    class ActiveQueries {
        @Test
        @DisplayName("isActiveThisWeek() retorna true para tipos seleccionados")
        void isActiveThisWeek_trueForSelectedTypes() {
            List<WeeklyQuestType> active = WeeklyQuestPool.selectForCurrentWeek();

            for (WeeklyQuestType type : active) {
                assertTrue(WeeklyQuestPool.isActiveThisWeek(type));
            }
        }

        @Test
        @DisplayName("isActiveForWeek() con null lanza NullPointerException")
        void isActiveForWeek_withNullType_throwsException() {
            assertThrows(NullPointerException.class,
                    () -> WeeklyQuestPool.isActiveForWeek(null, LocalDate.now()));
        }

        @Test
        @DisplayName("getInactiveForCurrentWeek() retorna exactamente 3 tipos")
        void getInactiveForCurrentWeek_returnsThreeTypes() {
            List<WeeklyQuestType> inactive = WeeklyQuestPool.getInactiveForCurrentWeek();

            assertEquals(3, inactive.size());
        }

        @Test
        @DisplayName("Active + Inactive = todos los tipos")
        void activeAndInactive_coversAllTypes() {
            LocalDate date = LocalDate.of(2026, 1, 28);

            List<WeeklyQuestType> active = WeeklyQuestPool.selectForWeek(date);
            List<WeeklyQuestType> inactive = WeeklyQuestPool.getInactiveForWeek(date);

            Set<WeeklyQuestType> all = new HashSet<>();
            all.addAll(active);
            all.addAll(inactive);

            assertEquals(6, all.size());
            assertTrue(all.containsAll(List.of(WeeklyQuestType.values())));
        }

        @Test
        @DisplayName("Active e Inactive no tienen intersección")
        void activeAndInactive_noIntersection() {
            LocalDate date = LocalDate.of(2026, 1, 28);

            List<WeeklyQuestType> active = WeeklyQuestPool.selectForWeek(date);
            List<WeeklyQuestType> inactive = WeeklyQuestPool.getInactiveForWeek(date);

            for (WeeklyQuestType type : active) {
                assertFalse(inactive.contains(type));
            }
        }
    }

    @Nested
    class RotationInfo {
        @Test
        @DisplayName("getRotationInfo() retorna información correcta")
        void getRotationInfo_returnsCorrectInfo() {
            LocalDate wednesday = LocalDate.of(2026, 1, 28);
            LocalDate expectedMonday = LocalDate.of(2026, 1, 26);
            LocalDate expectedSunday = LocalDate.of(2026, 2, 1);
            LocalDate expectedNextMonday = LocalDate.of(2026, 2, 2);

            WeekRotationInfo info = WeeklyQuestPool.getRotationInfo(wednesday);

            assertEquals(expectedMonday, info.weekStart());
            assertEquals(expectedSunday, info.weekEnd());
            assertEquals(expectedNextMonday, info.nextRotationDate());
            assertEquals(5, info.daysRemaining()); // Miércoles a Domingo = 5 días (Wed, Thu, Fri, Sat, Sun)
            assertEquals(3, info.currentQuests().size());
            assertEquals(3, info.nextWeekQuests().size());
        }

        @Test
        @DisplayName("getRotationInfo() desde Lunes tiene 7 días restantes")
        void getRotationInfo_fromMonday_has7DaysRemaining() {
            LocalDate monday = LocalDate.of(2026, 1, 26);

            WeekRotationInfo info = WeeklyQuestPool.getRotationInfo(monday);

            assertEquals(7, info.daysRemaining());
        }

        @Test
        @DisplayName("getRotationInfo() desde Domingo tiene 1 día restante")
        void getRotationInfo_fromSunday_has1DayRemaining() {
            LocalDate sunday = LocalDate.of(2026, 2, 1);

            WeekRotationInfo info = WeeklyQuestPool.getRotationInfo(sunday);

            assertEquals(1, info.daysRemaining());
        }
    }

    @Nested
    class WeekRotationInfoRecord {
        @Test
        @DisplayName("formatDaysRemaining() formatea correctamente")
        void formatDaysRemaining_formatsCorrectly() {
            LocalDate monday = LocalDate.of(2026, 1, 26);
            LocalDate saturday = LocalDate.of(2026, 1, 31);
            LocalDate sunday = LocalDate.of(2026, 2, 1);

            WeekRotationInfo infoMonday = WeeklyQuestPool.getRotationInfo(monday);
            WeekRotationInfo infoSaturday = WeeklyQuestPool.getRotationInfo(saturday);
            WeekRotationInfo infoSunday = WeeklyQuestPool.getRotationInfo(sunday);

            assertEquals("7 días restantes", infoMonday.formatDaysRemaining());
            assertEquals("2 días restantes", infoSaturday.formatDaysRemaining());
            assertEquals("Último día", infoSunday.formatDaysRemaining());
        }

        @Test
        @DisplayName("isLastDay() detecta el último día")
        void isLastDay_detectsLastDay() {
            LocalDate saturday = LocalDate.of(2026, 1, 31);
            LocalDate sunday = LocalDate.of(2026, 2, 1);

            assertFalse(WeeklyQuestPool.getRotationInfo(saturday).isLastDay());
            assertTrue(WeeklyQuestPool.getRotationInfo(sunday).isLastDay());
        }

        @Test
        @DisplayName("isRotationDay() detecta día de rotación")
        void isRotationDay_detectsRotationDay() {
            // El día de rotación sería cuando daysRemaining = 0
            // Esto ocurre cuando la fecha de referencia es después del domingo
            LocalDate sundayOfWeek = LocalDate.of(2026, 2, 1);
            LocalDate nextMonday = LocalDate.of(2026, 2, 2);

            // Desde el punto de vista del Domingo, aún queda 1 día
            assertFalse(WeeklyQuestPool.getRotationInfo(sundayOfWeek).isRotationDay());

            // Pero si creamos info para la semana pasada vista desde el lunes siguiente...
            // La rotación ya ocurrió
        }
    }

    @Nested
    class Validation {
        @Test
        @DisplayName("isValidSelection() = true para selección válida")
        void isValidSelection_trueForValidSelection() {
            List<WeeklyQuestType> valid = List.of(
                    WeeklyQuestType.HIBERNATOR,
                    WeeklyQuestType.IRON_WEEK,
                    WeeklyQuestType.THE_STREAK
            );

            assertTrue(WeeklyQuestPool.isValidSelection(valid));
        }

        @Test
        @DisplayName("isValidSelection() = false para null")
        void isValidSelection_falseForNull() {
            assertFalse(WeeklyQuestPool.isValidSelection(null));
        }

        @Test
        @DisplayName("isValidSelection() = false para lista vacía")
        void isValidSelection_falseForEmptyList() {
            assertFalse(WeeklyQuestPool.isValidSelection(List.of()));
        }

        @Test
        @DisplayName("isValidSelection() = false para lista con menos de 3")
        void isValidSelection_falseForLessThanThree() {
            List<WeeklyQuestType> tooFew = List.of(
                    WeeklyQuestType.HIBERNATOR,
                    WeeklyQuestType.IRON_WEEK
            );

            assertFalse(WeeklyQuestPool.isValidSelection(tooFew));
        }

        @Test
        @DisplayName("isValidSelection() = false para lista con más de 3")
        void isValidSelection_falseForMoreThanThree() {
            List<WeeklyQuestType> tooMany = List.of(
                    WeeklyQuestType.HIBERNATOR,
                    WeeklyQuestType.IRON_WEEK,
                    WeeklyQuestType.THE_STREAK,
                    WeeklyQuestType.CODE_MARATHON
            );

            assertFalse(WeeklyQuestPool.isValidSelection(tooMany));
        }

        @Test
        @DisplayName("isValidSelection() = false para lista con duplicados")
        void isValidSelection_falseForDuplicates() {
            List<WeeklyQuestType> withDuplicates = List.of(
                    WeeklyQuestType.HIBERNATOR,
                    WeeklyQuestType.HIBERNATOR,
                    WeeklyQuestType.IRON_WEEK
            );

            assertFalse(WeeklyQuestPool.isValidSelection(withDuplicates));
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("Cualquier combinación válida de 3 tipos pasa validación")
        void anyValidCombination_passesValidation(WeeklyQuestType first) {
            // Tomar el primer tipo y otros 2 diferentes
            List<WeeklyQuestType> allTypes = List.of(WeeklyQuestType.values());
            List<WeeklyQuestType> selection = allTypes.stream()
                    .filter(t -> t != first)
                    .limit(2)
                    .toList();

            List<WeeklyQuestType> fullSelection = List.of(
                    first,
                    selection.get(0),
                    selection.get(1)
            );

            assertTrue(WeeklyQuestPool.isValidSelection(fullSelection));
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Selección funciona para fechas muy antiguas")
        void selection_worksForOldDates() {
            LocalDate oldDate = LocalDate.of(2020, 1, 1);

            List<WeeklyQuestType> selected = WeeklyQuestPool.selectForWeek(oldDate);

            assertEquals(3, selected.size());
        }

        @Test
        @DisplayName("Selección funciona para fechas futuras")
        void selection_worksForFutureDates() {
            LocalDate futureDate = LocalDate.of(2030, 12, 25);

            List<WeeklyQuestType> selected = WeeklyQuestPool.selectForWeek(futureDate);

            assertEquals(3, selected.size());
        }

        @Test
        @DisplayName("Selección es consistente incluso cruzando años")
        void selection_consistentAcrossYears() {
            // Semana que cruza del 2025 al 2026
            LocalDate dec30 = LocalDate.of(2025, 12, 30); // Martes
            LocalDate jan2 = LocalDate.of(2026, 1, 2);    // Viernes

            List<WeeklyQuestType> fromDec30 = WeeklyQuestPool.selectForWeek(dec30);
            List<WeeklyQuestType> fromJan2 = WeeklyQuestPool.selectForWeek(jan2);

            // Ambas fechas están en la misma semana (Lunes 29 Dec - Domingo 4 Jan)
            assertEquals(fromDec30, fromJan2);
        }
    }
}
