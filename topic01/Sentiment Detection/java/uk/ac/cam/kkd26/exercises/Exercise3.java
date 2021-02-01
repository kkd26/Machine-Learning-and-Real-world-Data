package uk.ac.cam.kkd26.exercises;


import uk.ac.cam.cl.mlrd.utils.BestFit;
import uk.ac.cam.cl.mlrd.utils.BestFit.Point;
import uk.ac.cam.cl.mlrd.utils.BestFit.Line;
import uk.ac.cam.cl.mlrd.utils.ChartPlotter;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise3 {

	private Map<String, Word> frequencies = new HashMap<>();
	private long tokenCount = 0L;
	private long currentPower = 1L;
	private List<Word> sortedWordList;
	private List<Point> heapsPoints = new LinkedList<>();
	private List<Point> zipfPoints;
	private Line bestFitZipf = null;
	private Line bestFitHeaps = null;

	public Exercise3(Path reviews) throws IOException {
		init(reviews);
	}

	private void init(Path reviews) throws IOException {
		wordFrequenciesFromDirectory(reviews);
		sortedWordList = sortFrequencies();
		createRanks(sortedWordList);
		zipfPoints = getPoints(10000);
		makeFitZipf();
		makeFitHeaps();
	}

	private void wordFrequenciesFromDirectory(Path directory) throws IOException {
		int filesNum = new File(directory.toString()).listFiles().length;

		try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
			int i = 0;
			System.out.println("Processing files from directory:");
			for (Path file : files) {
				wordFrequenciesFromFile(file);
				displayProgress(++i * 1.0 / filesNum);
			}

			for (Word token : frequencies.values()) {
				token.calcFrequency(tokenCount);
			}

			System.out.println(" completed\n");
		} catch (IOException e) {
			throw new IOException("Can't read the reviews.", e);
		}
	}

	private void wordFrequenciesFromFile(Path path) throws IOException {
		long wordCount = 0L;

		List<String> words = tokenize(path);
		for (String word : words) {
			Word current = frequencies.getOrDefault(word, new Word(word));
			current.inc();
			wordCount++;
			frequencies.put(word, current);
			tokenCount++;
			if (tokenCount == currentPower) {
				currentPower <<= 1;
				heapsPoints.add(new Point(tokenCount, frequencies.size()));
			}
		}

		for (String word : words) {
			Word current = frequencies.getOrDefault(word, new Word(word));
			current.calcFrequency(wordCount);
		}
	}

	private void displayProgress(double d) {
		double progress = d * 100;
		int size = 50;
		StringBuilder s = new StringBuilder("\r[");

		for (int i = 0; i < size; i++) {
			if (i * 100.0 / size < progress) s.append('#');
			else s.append('.');
		}

		s.append(String.format("] %.2f%%", progress));
		System.out.print(s);
	}

	private List<Word> sortFrequencies() {
		Comparator<Word> wordComparator = (o1, o2) -> (int) (o2.getFrequency().compareTo(o1.getFrequency()));
		List<Word> wordList = new ArrayList<>(frequencies.values());
		wordList.sort(wordComparator);
		return wordList;
	}

	private void createRanks(List<Word> wordList) {
		long rank = 1L;
		for (Word token : wordList) {
			token.setRank(rank++);
		}
	}

	private void makeFitZipf() {
		Map<Point, Double> series = new HashMap<>();
		zipfPoints.forEach(point -> series.put(convertToLog(point), point.y));
		bestFitZipf = BestFit.leastSquares(series);
	}

	private void makeFitHeaps() {
		Map<Point, Double> series = new HashMap<>();
		heapsPoints.forEach(point -> series.put(convertToLog(point), 1.0));
		bestFitHeaps = BestFit.leastSquares(series);
	}

	private void displayFrequencies() throws IOException {
		List<Point> myPoints = getPoints(Paths.get("data/personal_sentiment_words"));
		ChartPlotter.plotLines(zipfPoints, myPoints);
	}

	private void displayFittedLine(List<Point> points, Line line) {
		List<Point> pointLine = points.stream().map(p -> new Point(p.x, line.gradient * p.x + line.yIntercept)).collect(Collectors.toList());
		ChartPlotter.plotLines(points, pointLine);
	}

	private Point convertToLog(Point p) {
		return new Point(Math.log(p.x), Math.log(p.y));
	}

	private List<Point> convertToLog(List<Point> points) {
		return points.stream().map(this::convertToLog).collect(Collectors.toList());
	}

	private List<Point> getPoints(long n) {
		List<Point> points = new LinkedList<>();

		for (int i = 0; i < n; i++) {
			Word token = sortedWordList.get(i);
			Point point = new Point(token.getRank(), token.getFrequency());
			points.add(point);
		}
		return points;
	}

	private List<Point> getPoints(Path path) throws IOException {
		List<Point> myPoints = new LinkedList<>();
		Map<String, Word> myLexicon = Exercise1.getLexicon(path);
		for (String word : myLexicon.keySet()) {
			Word token = frequencies.get(word);
			Point point = new Point(token.getRank(), token.getFrequency());
			myPoints.add(point);
		}
		return myPoints;
	}

	private double predictFreqFromRank(Word token) {
		return Math.exp(bestFitZipf.gradient * Math.log(token.getRank()) + bestFitZipf.yIntercept);
	}

	private void printFrequencies(Path path) throws IOException {
		Map<String, Word> myLexicon = Exercise1.getLexicon(path);
		System.out.println("Actual and predicted frequencies for my lexicon:");
		System.out.printf("%-12s%-12s%-12s\n", "[word]", "[act freq]", "[pred freq]");
		myLexicon.keySet().forEach(w -> {
				Word token = frequencies.get(w);
				double predFreq = predictFreqFromRank(token);
				System.out.printf("%-12s%-12f%-12f\n", token.getWord(), token.getFrequency(), predFreq);
			}
		);
		System.out.println();
	}

	private void displayZipfLaw() {
		displayFittedLine(convertToLog(zipfPoints), bestFitZipf);
	}

	private void displayZipfParams() {
		// f * r^a = k
		// log(f) + a*log(r) = log(k)
		// log(f) = -a*log(r) + log(k)
		System.out.println("Zipf's parameters:");
		System.out.printf("%-12s%-12s\n", "[alpha]", "[k]");
		System.out.printf("%-12f%-12f\n\n", -bestFitZipf.gradient, Math.exp(bestFitZipf.yIntercept));
	}

	private void displayHeapsLaw() {
		displayFittedLine(convertToLog(heapsPoints), bestFitHeaps);
	}

	private void displayHeapsParams() {
		// u = k * n^b
		// log(u) = log(k) + b*log(n)
		System.out.println("Heaps' parameters:");
		System.out.printf("%-12s%-12s\n", "[beta]", "[k]");
		System.out.printf("%-12f%-12f\n\n", bestFitHeaps.gradient, Math.exp(bestFitHeaps.yIntercept));
	}

	public static void main(String[] args) throws IOException {
		Path reviews = Paths.get("data/large_dataset");
		Exercise3 exercise3 = new Exercise3(reviews);

		// Zipf's Law
		exercise3.displayFrequencies();
		exercise3.displayZipfLaw();
		exercise3.displayZipfParams();

		exercise3.printFrequencies(Paths.get("data/personal_sentiment_words"));

		// Heaps' Law
		exercise3.displayHeapsLaw();
		exercise3.displayHeapsParams();
	}
}
