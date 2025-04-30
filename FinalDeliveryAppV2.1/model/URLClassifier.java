package model;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class URLClassifier {
    private Classifier classifier;
    private Instances datasetModel;

    public URLClassifier(String modelPath) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath));
        classifier = (Classifier) ois.readObject();
        ois.close();

        // Define 13 attributes (same order and names as ARFF header)
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("url_length"));
        attributes.add(new Attribute("has_ip_address"));
        attributes.add(new Attribute("has_https"));
        attributes.add(new Attribute("num_dots"));
        attributes.add(new Attribute("has_at_symbol"));
        attributes.add(new Attribute("url_depth"));
        attributes.add(new Attribute("is_shortened_url"));
        attributes.add(new Attribute("has_suspicious_words"));
        attributes.add(new Attribute("suspicious_word_count"));
        attributes.add(new Attribute("has_encoded_chars"));
        attributes.add(new Attribute("num_subdomains"));
        attributes.add(new Attribute("domain_entropy"));
        attributes.add(new Attribute("has_port_number"));

        // Define class attribute
        ArrayList<String> classVals = new ArrayList<>();
        classVals.add("good");
        classVals.add("bad");
        Attribute classAttribute = new Attribute("type", classVals);
        attributes.add(classAttribute);

        datasetModel = new Instances("url_features", attributes, 0);
        datasetModel.setClassIndex(datasetModel.numAttributes() - 1);
    }

    public String classify(String url) throws Exception {
        double[] features = URLFeatureExtractor.extractFeatures(url);
        return classify(url, features);
    }

    /**
     * Overloaded classify method that accepts pre-computed features
     * This allows for feature modification before classification
     */
    public String classify(String url, double[] features) throws Exception {
        Instance instance = new DenseInstance(features.length + 1);

        for (int i = 0; i < features.length; i++) {
            instance.setValue(i, features[i]);
        }

        instance.setDataset(datasetModel);
        double result = classifier.classifyInstance(instance);
        return datasetModel.classAttribute().value((int) result);
    }

    public double[] getDistribution(String url) throws Exception {
        double[] features = URLFeatureExtractor.extractFeatures(url);
        return getDistribution(url, features);
    }

    /**
     * Overloaded getDistribution method that accepts pre-computed features
     * This allows for feature modification before classification
     */
    public double[] getDistribution(String url, double[] features) throws Exception {
        Instance instance = new DenseInstance(features.length + 1);

        for (int i = 0; i < features.length; i++) {
            instance.setValue(i, features[i]);
        }

        instance.setDataset(datasetModel);
        return classifier.distributionForInstance(instance);
    }

    public String getClassLabel(int index) {
        return datasetModel.classAttribute().value(index);
    }
}