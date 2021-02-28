package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import uk.ac.cam.kkd26.exercises.Exercise7;
import uk.ac.cam.kkd26.exercises.Exercise8;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise8;

public class Exercise8Tester {

	static final Path dataDirectory = Paths.get("data/dice_dataset");

	private static void crossValidate(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> devSet) throws IOException {
		IExercise8 implementation = (IExercise8) new Exercise8();
		Map<List<DiceType>, List<DiceType>> true2PredictedMap = implementation.predictAll(model, devSet);

		System.out.println("------------------CV------------------");

		double precision = implementation.precision(true2PredictedMap);
		System.out.printf("Prediction precision:\t%f\n", precision);

		double recall = implementation.recall(true2PredictedMap);
		System.out.printf("Prediction recall:\t\t%f\n", recall);

		double fOneMeasure = implementation.fOneMeasure(true2PredictedMap);
		System.out.printf("Prediction fOneMeasure:\t%f\n", fOneMeasure);
		System.out.println("--------------------------------------");
	}

	public static void main(String[] args)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		List<Path> sequenceFiles = new ArrayList<>();
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
			for (Path item : files) {
				sequenceFiles.add(item);
			}
		} catch (IOException e) {
			throw new IOException("Cant access the dataset.", e);
		}

		// Use for testing the code
		Collections.shuffle(sequenceFiles, new Random(0));
		int testSize = sequenceFiles.size() / 10;
		List<Path> devSet = sequenceFiles.subList(0, testSize);
		List<Path> testSet = sequenceFiles.subList(testSize, 2 * testSize);
		List<Path> trainingSet = sequenceFiles.subList(testSize * 2, sequenceFiles.size());

		IExercise7 implementation7 = (IExercise7) new Exercise7();
		HiddenMarkovModel<DiceRoll, DiceType> model = implementation7.estimateHMM(trainingSet);

		crossValidate(model, devSet);

		IExercise8 implementation = (IExercise8) new Exercise8();

		HMMDataStore<DiceRoll, DiceType> data = HMMDataStore.loadDiceFile(testSet.get(0));
		List<DiceType> predicted = implementation.viterbi(model, data.observedSequence);
		System.out.println("True hidden sequence:");
		System.out.println(data.hiddenSequence);
		System.out.println();

		System.out.println("Predicted hidden sequence:");
		System.out.println(predicted);
		System.out.println();

		Map<List<DiceType>, List<DiceType>> true2PredictedMap = implementation.predictAll(model, testSet);
		System.out.println("-----------------TEST-----------------");

		double precision = implementation.precision(true2PredictedMap);
		System.out.printf("Prediction precision:\t%f\n", precision);

		double recall = implementation.recall(true2PredictedMap);
		System.out.printf("Prediction recall:\t\t%f\n", recall);

		double fOneMeasure = implementation.fOneMeasure(true2PredictedMap);
		System.out.printf("Prediction fOneMeasure:\t%f\n", fOneMeasure);
		System.out.println("--------------------------------------");
	}
}
