package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise1 implements IExercise1 {

	private static Map<String, Word> lexicon = null;

	private static void loadLexicon(Path lexiconPath, Boolean force) throws IOException {
		if (!force && lexicon != null) return;

		lexicon = new HashMap<String, Word>();
		try (BufferedReader reader = Files.newBufferedReader(lexiconPath)) {
			reader.lines().forEach(new Consumer<String>() {
				@Override
				public void accept(String line) {
					Word word = new Word(line);
					lexicon.put(word.getWord(), word);
				}
			});
		} catch (IOException e) {
			throw new IOException("Can't access the file " + lexiconPath, e);
		}
	}

	public static Map<String, Word> getLexicon(Path lexiconPath) throws IOException {
		loadLexicon(lexiconPath, true);
		return lexicon;
	}

	private static Sentiment classifyReview(Path review, Boolean improved) throws IOException {
		double num = 0;

		try {
			List<String> tokenize = tokenize(review);
			for (String token : tokenize) {
				Word word = lexicon.getOrDefault(token, null);
				if (word != null) {
					num += improved ? word.calcValueImproved() : word.calcValue();
				}
			}
		} catch (IOException e) {
			return null;
		}
		return num < 0 ? Sentiment.NEGATIVE : Sentiment.POSITIVE;
	}

	@Override
	public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
		loadLexicon(lexiconFile, false);
		Map<Path, Sentiment> classifier = new HashMap<>();
		for (Path test : testSet) {
			Sentiment sen = classifyReview(test, false);
			classifier.put(test, sen);
		}
		return classifier;
	}

	@Override
	public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {
		Set<Path> keys = trueSentiments.keySet();
		double correct = 0.0;
		for (Path key : keys) {
			if (trueSentiments.get(key) == predictedSentiments.get(key)) correct++;
		}
		return correct / keys.size();
	}

	@Override
	public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
		loadLexicon(lexiconFile, false);
		Map<Path, Sentiment> classifier = new HashMap<>();
		for (Path test : testSet) {
			Sentiment sen = classifyReview(test, true);
			classifier.put(test, sen);
		}
		return classifier;
	}
}
