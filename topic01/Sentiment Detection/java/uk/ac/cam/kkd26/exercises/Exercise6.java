package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise6 implements IExercise6 {
	@Override
	public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) {
		Map<NuancedSentiment, Double> classProb = new HashMap<>();
		long n = trainingSet.size();
		for (NuancedSentiment s : NuancedSentiment.values()) {
			long count = trainingSet.keySet().stream().filter(p -> trainingSet.get(p).equals(s)).count();
			classProb.put(s, count * 1.0 / n);
		}
		return classProb;
	}

	private Map<NuancedSentiment, Long> getSentimentCount(Map<String, Map<NuancedSentiment, Long>> wordCount) {
		Map<NuancedSentiment, Long> sentimentCount = new HashMap<>();

		for (String word : wordCount.keySet()) {
			for (NuancedSentiment sentiment : NuancedSentiment.values()) {
				Long countByWord = wordCount.get(word).get(sentiment);
				Long current = sentimentCount.getOrDefault(sentiment, 0L);
				sentimentCount.put(sentiment, current + countByWord);
			}
		}
		return sentimentCount;
	}

	private Map<String, Map<NuancedSentiment, Double>> calculateProbs(Map<String, Map<NuancedSentiment, Long>> wordCount) {
		Map<NuancedSentiment, Long> sentimentCount = getSentimentCount(wordCount);
		Map<String, Map<NuancedSentiment, Double>> toReturn = new HashMap<>();

		for (String word : wordCount.keySet()) {
			Map<NuancedSentiment, Long> countByWord = wordCount.get(word);
			Map<NuancedSentiment, Double> probabilityMap = new HashMap<>();
			for (NuancedSentiment sentiment : NuancedSentiment.values()) {
				double prob = countByWord.get(sentiment) * 1.0 / sentimentCount.get(sentiment);
				probabilityMap.put(sentiment, Math.log(prob));
			}
			toReturn.put(word, probabilityMap);
		}

		return toReturn;
	}

	private Map<String, Map<NuancedSentiment, Long>> getWordCount(Map<Path, NuancedSentiment> set, Long defaultValue) throws IOException {
		Map<String, Map<NuancedSentiment, Long>> wordCount = new HashMap<>();
		Map<NuancedSentiment, Long> sentimentMap = new HashMap<>();
		for (NuancedSentiment s : NuancedSentiment.values()) sentimentMap.put(s, defaultValue);

		for (Path p : set.keySet()) {
			List<String> words = tokenize(p);
			NuancedSentiment sentiment = set.get(p);
			for (String word : words) {
				Map<NuancedSentiment, Long> countMap = wordCount.getOrDefault(word, new HashMap<>(sentimentMap));
				Long countByWord = countMap.get(sentiment) + 1L;

				countMap.put(sentiment, countByWord);
				wordCount.put(word, countMap);
			}
		}
		return wordCount;
	}

	@Override
	public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {
		Map<String, Map<NuancedSentiment, Long>> wordCount = getWordCount(trainingSet, 1L);
		return calculateProbs(wordCount);
	}

	@Override
	public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {
		Map<Path, NuancedSentiment> result = new HashMap<>();
		for (Path path : testSet) {
			List<String> words = tokenize(path);
			double maxProb = -Double.MAX_VALUE;
			NuancedSentiment maxArg = NuancedSentiment.POSITIVE;
			for (NuancedSentiment sentiment : NuancedSentiment.values()) {
				Double sum = Math.log(classProbabilities.get(sentiment));
				for (String word : words) {
					sum += tokenLogProbs.getOrDefault(word, new HashMap<>()).getOrDefault(sentiment, 0.0);
				}
				if (sum > maxProb) {
					maxProb = sum;
					maxArg = sentiment;
				}
			}
			result.put(path, maxArg);
		}
		return result;
	}

	@Override
	public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments, Map<Path, NuancedSentiment> predictedSentiments) {
		Set<Path> keys = trueSentiments.keySet();
		double correct = 0.0;
		for (Path key : keys) {
			if (trueSentiments.get(key) == predictedSentiments.get(key)) correct++;
		}
		return correct / keys.size();
	}

	@Override
	public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {
		Map<Integer, Map<Sentiment, Integer>> result = new HashMap<>();

		for (Map<Integer, Sentiment> predictedSentiment : predictedSentiments) {
			for (int i : predictedSentiment.keySet()) {
				Sentiment sentiment = predictedSentiment.get(i);
				Map<Sentiment, Integer> currentReview = result.getOrDefault(i, new HashMap<>());
				currentReview.put(sentiment, currentReview.getOrDefault(sentiment, 0) + 1);
				result.put(i, currentReview);
			}
		}
		return result;
	}

	private double P_e(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
		int N = agreementTable.size();

		double P_e = 0.0;

		for (Sentiment sentiment : Sentiment.values()) {
			double p_j = 0.0;
			for (Map<Sentiment, Integer> sentimentIntegerMap : agreementTable.values()) {
				double n_i = 0.0;
				for (int n_ij : sentimentIntegerMap.values()) {
					n_i += n_ij;
				}

				int n_ij = sentimentIntegerMap.getOrDefault(sentiment, 0);
				p_j += n_ij / n_i;
			}

			p_j /= N;

			P_e += p_j * p_j;
		}

		return P_e;
	}

	private double P_a(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
		int N = agreementTable.size();

		double P_a = 0.0;

		for (Map<Sentiment, Integer> sentimentIntegerMap : agreementTable.values()) {
			int n_i = 0;

			for (int n_ij : sentimentIntegerMap.values()) {
				n_i += n_ij;
			}
			double sum = 0.0;
			for (int n_ij : sentimentIntegerMap.values()) {
				sum += n_ij * (n_ij - 1.0);
			}
			P_a += sum / (n_i * (n_i - 1.0));
		}
		P_a /= N;
		return P_a;
	}

	@Override
	public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
		double P_e = P_e(agreementTable);
		double P_a = P_a(agreementTable);
		System.out.println("P_e: " + P_e);
		System.out.println("P_a: " + P_a);
		return (P_a - P_e) / (1.0 - P_e);
	}
}
