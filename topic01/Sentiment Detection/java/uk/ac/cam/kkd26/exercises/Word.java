package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

public class Word {
	private final String word;
	private final Intensity intensity;
	private final Sentiment polarity;

	public Word(String line) {
		String[] tokens = line.split("\\s+");
		this.word = tokens[0].replace("word=", "");
		this.intensity = tokens[1].replace("intensity=", "").equals("weak") ? Intensity.WEAK : Intensity.STRONG;
		this.polarity = tokens[2].replace("polarity=", "").equals("positive") ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
	}

	public String getWord() {
		return word;
	}

	public Sentiment getPolarity() {
		return polarity;
	}

	public Intensity getIntensity() {
		return intensity;
	}

	public double calcValue(){
		return polarity == Sentiment.POSITIVE ? 1.0 : -1.0;
	}

	public double calcValueImproved(){
		return calcValue() * (intensity == Intensity.WEAK ? 0.5 : 1.0);
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
