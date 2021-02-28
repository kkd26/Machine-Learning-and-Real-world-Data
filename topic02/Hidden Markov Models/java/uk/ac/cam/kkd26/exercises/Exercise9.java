package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Exercise9 implements IExercise9 {
	private final Map<Feature, Map<Feature, Integer>> transitionCountMatrix = new HashMap<>();
	private final Map<Feature, Map<AminoAcid, Integer>> emissionCountMatrix = new HashMap<>();

	private int countTransitions(Feature a, Feature b, List<Feature> hiddenSequence) {
		boolean first = false;
		int count = 0;
		for (Feature diceType : hiddenSequence) {
			if (first && diceType == b) count++;
			first = diceType == a;
		}
		return count;
	}

	private void fillTransitionCountMatrix(HMMDataStore<AminoAcid, Feature> hmmDataStore) {
		List<Feature> hiddenSequence = hmmDataStore.hiddenSequence;
		for (Feature a : Feature.values()) {
			for (Feature b : Feature.values()) {
				int t = countTransitions(a, b, hiddenSequence);
				Map<Feature, Integer> aMap = transitionCountMatrix.getOrDefault(a, new HashMap<>());
				int value = aMap.getOrDefault(b, 0);
				aMap.put(b, value + t);
				transitionCountMatrix.put(a, aMap);
			}
		}
	}

	private int countEmissions(Feature a, AminoAcid b, List<Feature> hiddenSequence, List<AminoAcid> observedSequence) {
		return (int) IntStream.range(0, hiddenSequence.size()).filter(i -> hiddenSequence.get(i) == a && observedSequence.get(i) == b).count();
	}

	private void fillEmissionCountMatrix(HMMDataStore<AminoAcid, Feature> hmmDataStore) {
		List<Feature> hiddenSequence = hmmDataStore.hiddenSequence;
		List<AminoAcid> observedSequence = hmmDataStore.observedSequence;
		for (Feature a : Feature.values()) {
			for (AminoAcid b : AminoAcid.values()) {
				int t = countEmissions(a, b, hiddenSequence, observedSequence);
				Map<AminoAcid, Integer> aMap = emissionCountMatrix.getOrDefault(a, new HashMap<>());
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
	public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) {
		for (HMMDataStore<AminoAcid, Feature> sequencePair : sequencePairs) {
			fillTransitionCountMatrix(sequencePair);
			fillEmissionCountMatrix(sequencePair);
		}
		Map<Feature, Map<Feature, Double>> transitionMatrix = countMatrixToProbabilities(transitionCountMatrix);
		Map<Feature, Map<AminoAcid, Double>> emissionMatrix = countMatrixToProbabilities(emissionCountMatrix);

		return new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);
	}

	@Override
	public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
		return new Exercise8().viterbiAlg(model, observedSequence);
	}

	@Override
	public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) {
		Map<List<Feature>, List<Feature>> result = new HashMap<>();

		for (HMMDataStore<AminoAcid, Feature> testSequencePair : testSequencePairs) {
			List<Feature> predictedSequence = viterbi(model, testSequencePair.observedSequence);
			result.put(testSequencePair.hiddenSequence, predictedSequence);
		}
		return result;
	}

	private double measure(Map<List<Feature>, List<Feature>> true2PredictedMap, boolean isPrecision) {
		long size = 0, count = 0;
		for (Map.Entry<List<Feature>, List<Feature>> entry : true2PredictedMap.entrySet()) {
			List<Feature> trueSeq = entry.getKey();
			List<Feature> predSeq = entry.getValue();
			List<Feature> base = isPrecision ? predSeq : trueSeq;
			IntStream intStream = IntStream.range(0, trueSeq.size()).filter(i -> base.get(i) == Feature.MEMBRANE);
			size += intStream.count();
			IntStream intStream2 = IntStream.range(0, trueSeq.size()).filter(i -> trueSeq.get(i) == Feature.MEMBRANE);
			count += intStream2.filter(i -> trueSeq.get(i) == predSeq.get(i)).count();
		}
		return count * 1.0 / size;
	}

	@Override
	public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		return measure(true2PredictedMap, true);
	}

	@Override
	public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		return measure(true2PredictedMap, false);
	}

	@Override
	public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		double precision = precision(true2PredictedMap);
		double recall = recall(true2PredictedMap);
		return 2.0 * precision * recall / (precision + recall);
	}
}
