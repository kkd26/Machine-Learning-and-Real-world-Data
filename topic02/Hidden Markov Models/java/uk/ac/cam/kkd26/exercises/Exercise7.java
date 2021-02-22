package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Exercise7 implements IExercise7 {

	private final Map<DiceType, Map<DiceType, Integer>> transitionCountMatrix = new HashMap<>();
	private final Map<DiceType, Map<DiceRoll, Integer>> emissionCountMatrix = new HashMap<>();

	private int countTransitions(DiceType a, DiceType b, List<DiceType> hiddenSequence) {
		boolean first = false;
		int count = 0;
		for (DiceType diceType : hiddenSequence) {
			if (first && diceType == b) count++;
			first = diceType == a;
		}
		return count;
	}

	private void fillTransitionCountMatrix(HMMDataStore<DiceRoll, DiceType> hmmDataStore) {
		List<DiceType> hiddenSequence = hmmDataStore.hiddenSequence;
		for (DiceType a : DiceType.values()) {
			for (DiceType b : DiceType.values()) {
				int t = countTransitions(a, b, hiddenSequence);
				Map<DiceType, Integer> aMap = transitionCountMatrix.getOrDefault(a, new HashMap<>());
				int value = aMap.getOrDefault(b, 0);
				aMap.put(b, value + t);
				transitionCountMatrix.put(a, aMap);
			}
		}
	}

	private int countEmissions(DiceType a, DiceRoll b, List<DiceType> hiddenSequence, List<DiceRoll> observedSequence) {
		return (int) IntStream.range(0, hiddenSequence.size()).filter(i -> hiddenSequence.get(i) == a && observedSequence.get(i) == b).count();
	}

	private void fillEmissionCountMatrix(HMMDataStore<DiceRoll, DiceType> hmmDataStore) {
		List<DiceType> hiddenSequence = hmmDataStore.hiddenSequence;
		List<DiceRoll> observedSequence = hmmDataStore.observedSequence;
		for (DiceType a : DiceType.values()) {
			for (DiceRoll b : DiceRoll.values()) {
				int t = countEmissions(a, b, hiddenSequence, observedSequence);
				Map<DiceRoll, Integer> aMap = emissionCountMatrix.getOrDefault(a, new HashMap<>());
				int value = aMap.getOrDefault(b, 0);
				aMap.put(b, value + t);
				emissionCountMatrix.put(a, aMap);
			}
		}
	}

	private <T, U> Map<T, Map<U, Double>> countMatrixToProbabilities(Map<T, Map<U, Integer>> m) {
		return m.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
			Map<U, Integer> innerMap = e.getValue();
			int sum = innerMap.values().stream().reduce(0, Integer::sum);
			return innerMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, f -> sum != 0.0 ? f.getValue() * 1.0 / sum : 0.0));
		}));
	}

	@Override
	public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
		List<HMMDataStore<DiceRoll, DiceType>> hmmDataStores = HMMDataStore.loadDiceFiles(sequenceFiles);

		for (HMMDataStore<DiceRoll, DiceType> hmmDataStore : hmmDataStores) {
			fillTransitionCountMatrix(hmmDataStore);
			fillEmissionCountMatrix(hmmDataStore);
		}
		Map<DiceType, Map<DiceType, Double>> transitionMatrix = countMatrixToProbabilities(transitionCountMatrix);
		Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = countMatrixToProbabilities(emissionCountMatrix);

		return new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);
	}
}
