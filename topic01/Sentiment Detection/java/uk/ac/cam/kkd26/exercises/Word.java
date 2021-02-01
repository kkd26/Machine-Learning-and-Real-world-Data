package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

public class Word {
	private final String word;
	private final Intensity intensity;
	private final Sentiment polarity;
	private Long count = 0L;
	private Double frequency = 0.0;
	private Long rank = 0L;

	public Word(String line) {
		if (line.matches("^word=(.*)")) {
			String[] tokens = line.split("\\s+");
			this.word = tokens[0].replace("word=", "");
			this.intensity = tokens[1].replace("intensity=", "").equals("weak") ? Intensity.WEAK : Intensity.STRONG;
			this.polarity = tokens[2].replace("polarity=", "").equals("positive") ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
		} else {
			word = line;
			intensity = null;
			polarity = null;
		}
	}

	public String getWord() {
		return word;
	}

	public Intensity getIntensity() {
		return intensity;
	}

	public Sentiment getPolarity() {
		return polarity;
	}

	public Double getFrequency() {
		return frequency;
	}

	public Long getRank() {
		return rank;
	}

	public double calcValue() {
		return polarity == Sentiment.POSITIVE ? 1.0 : -1.0;
	}

	public double calcValueImproved() {
		return calcValue() * (intensity == Intensity.WEAK ? 0.5 : 1.0);
	}

	public void calcFrequency(long total) {
		frequency = count * 1.0 / total;
	}

	public void setRank(Long rank) {
		this.rank = rank;
	}

	public void inc() {
		this.count++;
	}

	@Override
	public String toString() {
		return "Word{" +
			"word='" + word + '\'' +
			", intensity=" + intensity +
			", polarity=" + polarity +
			'}';
	}
}
