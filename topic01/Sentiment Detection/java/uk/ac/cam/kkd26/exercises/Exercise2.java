package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise2 implements IExercise2 {
	@Override
	public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) {
		Map<Sentiment, Double> classProb = new HashMap<>();
		long n = trainingSet.size();
		for (Sentiment s : Sentiment.values()) {
			long count = trainingSet.keySet().stream().filter(p -> trainingSet.get(p).equals(s)).count();
			classProb.put(s, count * 1.0 / n);
		}
		return classProb;
	}

	private Map<String, Map<Sentiment, Long>> getWordCount(Map<Path, Sentiment> set, Long defaultValue) throws IOException {
		Map<String, Map<Sentiment, Long>> wordCount = new HashMap<>();
		Map<Sentiment, Long> sentimentMap = new HashMap<>();
		for (Sentiment s : Sentiment.values()) sentimentMap.put(s, defaultValue);

		for (Path p : set.keySet()) {
			List<String> words = tokenize(p);
			Sentiment sentiment = set.get(p);
			for (String word : words) {
				Map<Sentiment, Long> countMap = wordCount.getOrDefault(word, new HashMap<>(sentimentMap));
				Long countByWord = countMap.get(sentiment) + 1L;

				countMap.put(sentiment, countByWord);
				wordCount.put(word, countMap);
			}
		}
		return wordCount;
	}

	private Map<Sentiment, Long> getSentimentCount(Map<String, Map<Sentiment, Long>> wordCount) {
		Map<Sentiment, Long> sentimentCount = new HashMap<>();

		for (String word : wordCount.keySet()) {
			for (Sentiment sentiment : Sentiment.values()) {
				Long countByWord = wordCount.get(word).get(sentiment);
				Long current = sentimentCount.getOrDefault(sentiment, 0L);
				sentimentCount.put(sentiment, current + countByWord);
			}
		}
		return sentimentCount;
	}

	private Map<String, Map<Sentiment, Double>> calculateProbs(Map<String, Map<Sentiment, Long>> wordCount) {
		Map<Sentiment, Long> sentimentCount = getSentimentCount(wordCount);
		Map<String, Map<Sentiment, Double>> toReturn = new HashMap<>();

		for (String word : wordCount.keySet()) {
			Map<Sentiment, Long> countByWord = wordCount.get(word);
			Map<Sentiment, Double> probabilityMap = new HashMap<>();
			for (Sentiment sentiment : Sentiment.values()) {
				Double prob = countByWord.get(sentiment) * 1.0 / sentimentCount.get(sentiment);
				probabilityMap.put(sentiment, Math.log(prob));
			}
			toReturn.put(word, probabilityMap);
		}

		return toReturn;
	}

	@Override
	public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
		Map<String, Map<Sentiment, Long>> wordCount = getWordCount(trainingSet, 0L);
		return calculateProbs(wordCount);
	}

	@Override
	public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
		Map<String, Map<Sentiment, Long>> wordCount = getWordCount(trainingSet, 1L);
		return calculateProbs(wordCount);
	}

	@Override
	public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {
		Map<Path, Sentiment> result = new HashMap<>();
		for (Path path : testSet) {
			List<String> words = tokenize(path);
			Double maxProb = -Double.MAX_VALUE;
			Sentiment maxArg = Sentiment.POSITIVE;
			for (Sentiment sentiment : Sentiment.values()) {
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
}
