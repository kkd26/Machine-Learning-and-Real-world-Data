package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

public class Exercise4 implements IExercise4 {

	private static final BigInteger TWO = ONE.add(ONE);

	@Override
	public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
		Exercise1 classifier = new Exercise1();
		return classifier.improvedClassifier(testSet, lexiconFile);
	}

	@Override
	public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA, Map<Path, Sentiment> classificationB) {
		Set<Path> paths = actualSentiments.keySet();
		BigInteger Null = ZERO, Plus = ZERO, Minus = ZERO;

		for (Path path : paths) {
			Sentiment trueSentiment = actualSentiments.get(path);
			Sentiment ASentiment = classificationA.get(path);
			Sentiment BSentiment = classificationB.get(path);

			if (ASentiment == BSentiment) Null = Null.add(ONE);
			else if (trueSentiment == ASentiment) Plus = Plus.add(ONE);
			else Minus = Minus.add(ONE);
		}

		BigInteger base = (Null.add(ONE).divide(TWO));
		BigInteger n = base.multiply(TWO).add(Plus).add(Minus);
		BigInteger k = base.add(Plus.min(Minus));

		// ( n )        n!         (  n  )    (n-i+1)
		// (   ) = ------------- = (     ) * ---------
		// ( i )    (n-i)! * i!    ( i-1 )       i

		BigInteger coef = ONE;
		BigInteger sum = ONE;

		for (BigInteger i = ONE; i.compareTo(k) <= 0; i = i.add(ONE)) {
			coef = coef.multiply(n.subtract(i).add(ONE)).divide(i);
			sum = sum.add(coef);
		}

		return 2.0 * sum.doubleValue() * Math.pow(.5, n.doubleValue());
	}
}