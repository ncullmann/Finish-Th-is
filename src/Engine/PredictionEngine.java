package Engine;

import java.io.*;
import java.util.*;

public class PredictionEngine {

    private static PredictionEngine instance = null;
    private Map<String, Set<WordNode>> predictionMap;
    private Map<Map<String, String>, WordNode> stringToNode;
    private List<String> topAvailableWords;

    public PredictionEngine() {
        predictionMap = new HashMap<>();
        stringToNode = new HashMap<>();
        topAvailableWords = new ArrayList<>();
    }

    static PredictionEngine getInstance() {
        if (instance == null) instance = new PredictionEngine();
        return instance;
    }

    public void train(String firstWord, String secondWord) {
        if (firstWord.equals("") || secondWord.equals(""))
            return;

        if (predictionMap.containsKey(firstWord)) {
            var nodeKey = Map.of(firstWord, secondWord);
            WordNode node;

            if (stringToNode.containsKey(nodeKey)) {
                node = stringToNode.get(nodeKey);
                predictionMap.get(firstWord).remove(node);
                node.incrementFrequency();
            } else {
                node = new WordNode(secondWord);
            }
            predictionMap.get(firstWord).add(node);
            stringToNode.put(Map.of(firstWord, secondWord), node);
        } else {
            var node = new WordNode(secondWord);
            TreeSet<WordNode> set = new TreeSet<>();
            set.add(node);
            predictionMap.put(firstWord, set);
            stringToNode.put(Map.of(firstWord, secondWord), node);
        }
    }

    public void saveState() throws IOException {
        var pmOutStream = new FileOutputStream("Assets/pm.ser");
        var out = new ObjectOutputStream(pmOutStream);
        out.writeObject(predictionMap);
        pmOutStream.close();

        var stnOutStream = new FileOutputStream("Assets/stn.ser");
        out = new ObjectOutputStream(stnOutStream);
        out.writeObject(stringToNode);
        stnOutStream.close();
    }

    public void loadState() throws IOException, ClassNotFoundException {
        var pmInStream = new FileInputStream("Assets/pm.ser");
        var in = new ObjectInputStream(pmInStream);
        predictionMap = (Map<String, Set<WordNode>>) in.readObject();
        pmInStream.close();

        var stnInStream = new FileInputStream("Assets/stn.ser");
        in = new ObjectInputStream(stnInStream);
        stringToNode = (Map<Map<String, String>, WordNode>) in.readObject();
        stnInStream.close();
    }

    List<String> getAvailableWords(String firstWord, String secondWord) {
        topAvailableWords = new ArrayList<>();

        if (predictionMap.containsKey(firstWord)) {
            topAvailableWords = predictionMap.get(firstWord).stream().map(WordNode::getWord).filter(word -> word.startsWith(secondWord)).toList();
        } else {
            topAvailableWords = predictionMap.keySet().stream().filter(word -> word.startsWith(secondWord)).toList();
        }
        return topAvailableWords;
    }

    List<String> getAvailableWords() {
        return topAvailableWords;
    }

    List<String> getNextWords(String secondWord) {
        topAvailableWords = new ArrayList<>();
        if (predictionMap.containsKey(secondWord)) {
            var it = predictionMap.get(secondWord).iterator();
            for (int i = 0; i < 3 && it.hasNext(); i++) {
                topAvailableWords.add(it.next().getWord());
            }
        }
        return topAvailableWords;
    }
}