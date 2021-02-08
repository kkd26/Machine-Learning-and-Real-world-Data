package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise5 implements IExercise5 {
	@Override
	public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
		ArrayList<Path> pathArrayList = new ArrayList<>(dataSet.keySet());

		Collections.shuffle(pathArrayList, new Random(seed));

		List<Map<Path, Sentiment>> split = new ArrayList<>();

		int n = 10;
		for (int i = 0; i < n; i++) split.add(new HashMap<>());

		int i = 0;
		for (Path p : pathArrayList) {
			split.get(i++ % n).put(p, dataSet.get(p));
		}
		return split;
	}

	@Override
	public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
		ArrayList<Path> pathArrayList = new ArrayList<>(dataSet.keySet());

		Collections.shuffle(pathArrayList, new Random(seed));

		List<Map<Path, Sentiment>> split = new ArrayList<>();

		int n = 10;
		for (int i = 0; i < n; i++) split.add(new HashMap<>());

		Map<Sentiment, Integer> counters = new HashMap<>();
		for (Sentiment s : Sentiment.values()) counters.put(s, 0);

		for (Path p : pathArrayList) {
			Sentiment sentiment = dataSet.get(p);
			int num = counters.get(sentiment);
			counters.put(sentiment, num + 1);

			split.get(num % n).put(p, dataSet.get(p));
		}
		return split;
	}

	@Override
	public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
		int n = folds.size();
		double[] result = new double[n];

		IExercise2 NBClassifier = new Exercise2();
		Exercise1 simpleClassifier = new Exercise1();
		for (int i = 0; i < n; i++) {
			Map<Path, Sentiment> trainingSet = new HashMap<>();
			Map<Path, Sentiment> testSet = new HashMap<>(folds.get(i));
			for (int j = 0; j < n; j++) {
				if (j != i) {
					trainingSet.putAll(folds.get(j));
				}
			}
			Map<Sentiment, Double> classProbabilities = NBClassifier.calculateClassProbabilities(trainingSet);
			Map<String, Map<Sentiment, Double>> logProbs = NBClassifier.calculateSmoothedLogProbs(trainingSet);
			Map<Path, Sentiment> predictions = NBClassifier.naiveBayes(testSet.keySet(), logProbs, classProbabilities);

			double accuracy = simpleClassifier.calculateAccuracy(testSet, predictions);
			result[i] = accuracy;
		}

		return result;
	}

	@Override
	public double cvAccuracy(double[] scores) {
		int n = scores.length;
		double sum = 0.0;
		for (double score : scores) sum += score;
		return sum / n;
	}

	@Override
	public double cvVariance(double[] scores) {
		int n = scores.length;
		double sum = 0.0;

		double avg = cvAccuracy(scores);
		for (double score : scores) sum += (avg - score) * (avg - score);
		return sum / n;
	}
}
