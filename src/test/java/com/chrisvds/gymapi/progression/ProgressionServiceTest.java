package com.chrisvds.gymapi.progression;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ProgressionServiceTest {

	private final ProgressionService service = new ProgressionService();

	private Suggestion suggest(List<SetPerf> sets) {
		return service.suggest(sets, 6, 8, 5.0, "lb");
	}

	@Test
	void firstTimeWhenNoHistory() {
		assertThat(suggest(List.of()).action()).isEqualTo(Suggestion.FIRST_TIME);
		assertThat(suggest(null).action()).isEqualTo(Suggestion.FIRST_TIME);
	}

	@Test
	void increaseWhenAnySetWellOverTheTop() {
		// hit 10 on a top set of 8 -> definitely go up
		Suggestion s = suggest(List.of(
				new SetPerf(100, 10, true),
				new SetPerf(100, 8, true),
				new SetPerf(100, 8, true)));
		assertThat(s.action()).isEqualTo(Suggestion.INCREASE);
		assertThat(s.weight()).isEqualTo(105.0);
	}

	@Test
	void increaseWhenEverySetAtTheTop() {
		Suggestion s = suggest(List.of(
				new SetPerf(80, 8, true),
				new SetPerf(80, 8, true),
				new SetPerf(80, 8, true)));
		assertThat(s.action()).isEqualTo(Suggestion.INCREASE);
		assertThat(s.weight()).isEqualTo(85.0);
	}

	@Test
	void addRepsWhenInRangeButNotMaxed() {
		Suggestion s = suggest(List.of(
				new SetPerf(80, 7, true),
				new SetPerf(80, 6, true),
				new SetPerf(80, 6, true)));
		assertThat(s.action()).isEqualTo(Suggestion.ADD_REPS);
		assertThat(s.weight()).isEqualTo(80.0);
	}

	@Test
	void holdOrDeloadWhenMostSetsUnderTheRange() {
		Suggestion s = suggest(List.of(
				new SetPerf(90, 5, true),
				new SetPerf(90, 4, true),
				new SetPerf(90, 6, true)));
		assertThat(s.action()).isEqualTo(Suggestion.HOLD_OR_DELOAD);
	}

	@Test
	void holdWhenOneSetDipsButNotMost() {
		Suggestion s = suggest(List.of(
				new SetPerf(90, 7, true),
				new SetPerf(90, 7, true),
				new SetPerf(90, 5, true)));
		assertThat(s.action()).isEqualTo(Suggestion.HOLD);
	}

	@Test
	void ignoresUncheckedSetsWhenSomeAreChecked() {
		Suggestion s = suggest(List.of(
				new SetPerf(80, 8, true),
				new SetPerf(80, 8, true),
				new SetPerf(80, 12, false))); // warmup / not a working set
		assertThat(s.action()).isEqualTo(Suggestion.INCREASE);
		assertThat(s.weight()).isEqualTo(85.0);
	}

	@Test
	void fallsBackToAllSetsWhenNoneChecked() {
		Suggestion s = suggest(List.of(
				new SetPerf(50, 7, false),
				new SetPerf(50, 7, false)));
		assertThat(s.action()).isEqualTo(Suggestion.ADD_REPS);
	}

	@Test
	void bodyweightExercisesNeverGetAWeightBump() {
		Suggestion s = service.suggest(
				List.of(new SetPerf(0, 12, true), new SetPerf(0, 12, true)),
				6, 8, 0.0, "lb");
		assertThat(s.action()).isNotEqualTo(Suggestion.INCREASE);
	}
}
