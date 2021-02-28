package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;
import uk.ac.cam.kkd26.exercises.Exercise9;

public class Exercise9Tester {

	static final Path dataFile = Paths.get("data/bio_dataset.txt");

	private static void crossValidate(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> devSet) throws IOException {
		IExercise9 implementation = (IExercise9) new Exercise9();
		Map<List<Feature>, List<Feature>> true2PredictedMap = implementation.predictAll(model, devSet);

		System.out.println("------------------CV------------------");

		double precision = implementation.precision(true2PredictedMap);
		System.out.printf("Prediction precision:\t%f\n", precision);

		double recall = implementation.recall(true2PredictedMap);
		System.out.printf("Prediction recall:\t\t%f\n", recall);

		double fOneMeasure = implementation.fOneMeasure(true2PredictedMap);
		System.out.printf("Prediction fOneMeasure:\t%f\n", fOneMeasure);
		System.out.println("--------------------------------------");
	}

	public static void main(String[] args) throws IOException {

		List<HMMDataStore<AminoAcid, Feature>> sequencePairs = HMMDataStore.loadBioFile(dataFile);

		// Use for testing the code
		Collections.shuffle(sequencePairs, new Random(0));
		int testSize = sequencePairs.size() / 10;
		List<HMMDataStore<AminoAcid, Feature>> devSet = sequencePairs.subList(0, testSize);
		List<HMMDataStore<AminoAcid, Feature>> testSet = sequencePairs.subList(testSize, 2 * testSize);
		List<HMMDataStore<AminoAcid, Feature>> trainingSet = sequencePairs.subList(testSize * 2, sequencePairs.size());

		IExercise9 implementation = (IExercise9) new Exercise9();

		HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(trainingSet);

		crossValidate(model, devSet);

		System.out.println("Predicted transitions:");
		System.out.println(model.getTransitionMatrix());
		System.out.println();
		System.out.println("Predicted emissions:");
		System.out.println(model.getEmissionMatrix());
		System.out.println();

		HMMDataStore<AminoAcid, Feature> data = testSet.get(0);
		List<Feature> predicted = implementation.viterbi(model, data.observedSequence);
		System.out.println("True hidden sequence:");
		System.out.println(data.hiddenSequence);
		System.out.println();

		System.out.println("Predicted hidden sequence:");
		System.out.println(predicted);
		System.out.println();

		Map<List<Feature>, List<Feature>> true2PredictedSequences = implementation.predictAll(model, testSet);

		System.out.println("-----------------TEST-----------------");

		double precision = implementation.precision(true2PredictedSequences);
		System.out.printf("Prediction precision:\t%f\n", precision);

		double recall = implementation.recall(true2PredictedSequences);
		System.out.printf("Prediction recall:\t\t%f\n", recall);

		double fOneMeasure = implementation.fOneMeasure(true2PredictedSequences);
		System.out.printf("Prediction fOneMeasure:\t%f\n", fOneMeasure);
		System.out.println("--------------------------------------");
	}
}