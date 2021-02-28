package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

public class Exercise8 implements IExercise8 {

	private <T, R> List<T> viterbiAlg(HiddenMarkovModel<R, T> model, List<R> observedSequence) {
		int time = observedSequence.size();
		R observedStart = observedSequence.get(0);
		R observedEnd = observedSequence.get(time - 1);

		Set<T> hiddenStates = model.getHiddenStates();
		Map<T, Map<T, Double>> transitionMatrix = model.getTransitionMatrix();
		Map<T, Map<R, Double>> emissionMatrix = model.getEmissionMatrix();

		T hiddenStart = null;
		T hiddenEnd = null;

		Map<T, Map<Integer, Double>> delta = new HashMap<>();
		Map<T, Map<Integer, T>> psi = new HashMap<>();

		for (T hiddenState : hiddenStates) {
			delta.put(hiddenState, new HashMap<>());
			psi.put(hiddenState, new HashMap<>());
			if (emissionMatrix.get(hiddenState).get(observedStart) != 0.0) hiddenStart = hiddenState;
			if (emissionMatrix.get(hiddenState).get(observedEnd) != 0.0) hiddenEnd = hiddenState;
		}

		Map<Integer, Double> deltaStart = delta.get(hiddenStart);
		deltaStart.put(0, 0.0);
		delta.put(hiddenStart, deltaStart);

		for (int t = 1; t < time; t++) {
			R o = observedSequence.get(t);
			for (T j : hiddenStates) {
				double max = -Double.MAX_VALUE;
				if (emissionMatrix.get(j).get(o) == 0.0) continue;

				for (T i : hiddenStates) {
					if (transitionMatrix.get(i).get(j) == 0.0) continue;
					if (!delta.get(i).containsKey(t - 1)) continue;

					double d = delta.get(i).get(t - 1) + Math.log(transitionMatrix.get(i).get(j)) + Math.log(emissionMatrix.get(j).get(o));
					if (d > max) {
						max = d;
						Map<Integer, T> currentPsi = psi.get(j);
						currentPsi.put(t, i);
						psi.put(j, currentPsi);
					}
				}
				Map<Integer, Double> currentDelta = delta.get(j);
				currentDelta.put(t, max);
				delta.put(j, currentDelta);
			}
		}

		T hiddenCurrent = hiddenEnd;
		List<T> result = new LinkedList<>();
		for (int t = time - 1; t > 0; t--) {
			result.add(hiddenCurrent);
			hiddenCurrent = psi.get(hiddenCurrent).get(t);
		}
		result.add(hiddenCurrent);
		Collections.reverse(result);

		return result;
	}

	@Override
	public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {
		return viterbiAlg(model, observedSequence);
	}

	@Override
	public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> testFiles) throws IOException {
		List<HMMDataStore<DiceRoll, DiceType>> hmmDataStores = HMMDataStore.loadDiceFiles(testFiles);

		Map<List<DiceType>, List<DiceType>> result = new HashMap<>();

		for (HMMDataStore<DiceRoll, DiceType> hmmDataStore : hmmDataStores) {
			List<DiceType> predictedSequence = viterbi(model, hmmDataStore.observedSequence);
			result.put(hmmDataStore.hiddenSequence, predictedSequence);
		}

		return result;
	}

	@Override
	public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		long size = 0, count = 0;
		for (Map.Entry<List<DiceType>, List<DiceType>> entry : true2PredictedMap.entrySet()) {
			List<DiceType> trueSeq = entry.getKey();
			List<DiceType> predSeq = entry.getValue();
			IntStream intStream = IntStream.range(0, trueSeq.size()).filter(i -> predSeq.get(i) == DiceType.WEIGHTED);
			size += intStream.count();
			IntStream intStream2 = IntStream.range(0, trueSeq.size()).filter(i -> trueSeq.get(i) == DiceType.WEIGHTED);
			count += intStream2.filter(i -> trueSeq.get(i) == predSeq.get(i)).count();
		}
		return count * 1.0 / size;
	}

	@Override
	public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		long size = 0, count = 0;
		for (Map.Entry<List<DiceType>, List<DiceType>> entry : true2PredictedMap.entrySet()) {
			List<DiceType> trueSeq = entry.getKey();
			List<DiceType> predSeq = entry.getValue();
			IntStream intStream = IntStream.range(0, trueSeq.size()).filter(i -> trueSeq.get(i) == DiceType.WEIGHTED);
			size += intStream.count();
			IntStream intStream2 = IntStream.range(0, trueSeq.size()).filter(i -> trueSeq.get(i) == DiceType.WEIGHTED);
			count += intStream2.filter(i -> trueSeq.get(i) == predSeq.get(i)).count();
		}
		return count * 1.0 / size;
	}

	@Override
	public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		double precision = precision(true2PredictedMap);
		double recall = recall(true2PredictedMap);
		return 2.0 * precision * recall / (precision + recall);
	}
}
