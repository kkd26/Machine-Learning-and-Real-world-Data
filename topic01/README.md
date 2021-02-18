# Sentiment detection

## Task 1 - Simple Classifier

The main task in this session is to use the sentiment lexicon to automatically classify a large number of film reviews as positive or negative.

## Task 2 - Naive Bayes classifier

A simple classifier which uses all words from a text and Bayes theorem to estimate maximum likelihood estimator for a specific sentiment.

## Task 3 - Zipf's and Heaps' laws visualisation

In this lab I showed Zipf's and Heaps' laws on graphs and estimated their parameters from the best fit line. For this task I used large dataset with 35566 texts.

## Task 4 - Statistical Testing

Comparing two classifiers using a p-value measurement for statistical significance

## Task 5 - Cross-validation and test sets

Splitting data into folds which will create train set and cross-validation set using random or stratified random technique. Calculating average accuracy and variance of a NB classifier using the training and validation sets.

Presenting The Wayne Rooney effect - testing how a NB classifier trained on previously obtained (old) data perform on a review data. Training on 2004 database and testing on 2016 database.

## Task 6 - Uncertainty and human agreement

Extending sentiment to Neutral, training classifier. Then calculating agreement table and Fleiss' kappa.

## Usage

Unpack `data.tar.gz` file and move it to `Sentiment Detection` directory. Then add `stanford-postagger.jar` and `JavaFX` libraries to the project so you can enjoy my code in `java.uk.ac.cam.kkd26.exercises`. More details are described in `task[1-6].pdf`.
