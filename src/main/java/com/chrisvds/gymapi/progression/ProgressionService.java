package com.chrisvds.gymapi.progression;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chrisvds.gymapi.common.Lifts;

/**
 * Double-progression coach. Given how the last session on an exercise went and
 * the target rep window, decides whether to add weight, chase reps, or hold.
 *
 * <p>Rules (working sets = those marked done, or all sets with reps &gt; 0 if
 * none are marked):
 * <ul>
 *   <li>any working set at {@code repMax + 2} or more → add weight now</li>
 *   <li>every working set at {@code repMax} or more → add weight</li>
 *   <li>all working sets at {@code repMin} or more → in range, add a rep</li>
 *   <li>more than half the working sets below {@code repMin} → hold, deload if it repeats</li>
 *   <li>otherwise → hold and push for reps</li>
 * </ul>
 */
@Service
public class ProgressionService {

	public Suggestion suggest(List<SetPerf> lastSession, int repMin, int repMax, double increment, String unit) {
		String u = unit == null ? "lb" : unit;

		List<SetPerf> working = pickWorking(lastSession);
		if (working.isEmpty()) {
			return new Suggestion(Suggestion.FIRST_TIME, null,
					"First time logging this — pick a weight you can do for " + repMin + "–" + repMax + ".");
		}

		double topWeight = working.stream().mapToDouble(SetPerf::weight).max().orElse(0);
		int maxReps = working.stream().mapToInt(SetPerf::reps).max().orElse(0);
		int minReps = working.stream().mapToInt(SetPerf::reps).min().orElse(0);
		long belowMin = working.stream().filter(s -> s.reps() < repMin).count();
		double next = Lifts.round(topWeight + increment, 2);

		if (increment > 0 && maxReps >= repMax + 2) {
			return new Suggestion(Suggestion.INCREASE, next,
					"You hit " + maxReps + " reps last time — move up to " + fmt(next) + " " + u + ".");
		}
		if (increment > 0 && minReps >= repMax) {
			return new Suggestion(Suggestion.INCREASE, next,
					"All sets at the top of the range — add weight: " + fmt(next) + " " + u + ".");
		}
		if (minReps >= repMin) {
			return new Suggestion(Suggestion.ADD_REPS, Lifts.round(topWeight, 2),
					"In range at " + fmt(topWeight) + " " + u + ". Aim for " + repMax
							+ " on every set, then add weight.");
		}
		if (belowMin * 2 > working.size()) {
			return new Suggestion(Suggestion.HOLD_OR_DELOAD, Lifts.round(topWeight, 2),
					"Under target last time. Stay at " + fmt(topWeight) + " " + u
							+ "; if it happens again, drop about 10%.");
		}
		return new Suggestion(Suggestion.HOLD, Lifts.round(topWeight, 2),
				"Hold " + fmt(topWeight) + " " + u + " and push for more reps.");
	}

	private List<SetPerf> pickWorking(List<SetPerf> sets) {
		if (sets == null || sets.isEmpty()) {
			return List.of();
		}
		List<SetPerf> done = sets.stream().filter(SetPerf::done).filter(s -> s.reps() > 0).toList();
		if (!done.isEmpty()) {
			return done;
		}
		return sets.stream().filter(s -> s.reps() > 0 && s.weight() > 0).toList();
	}

	private String fmt(double n) {
		return n == Math.floor(n) ? String.valueOf((long) n) : String.valueOf(n);
	}
}
