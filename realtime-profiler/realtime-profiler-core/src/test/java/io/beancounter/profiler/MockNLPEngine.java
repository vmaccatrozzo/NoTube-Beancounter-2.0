package io.beancounter.profiler;

import io.beancounter.commons.nlp.*;

import java.net.URL;
import java.util.Random;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockNLPEngine implements NLPEngine {

    private Random random = new Random();

    @Override
    public NLPEngineResult enrich(String text) throws NLPEngineException {
        NLPEngineResult result = new NLPEngineResult();
        try {
            for (int i = 0; i < random.nextInt(3); i++) {
                result.addEntity(buildFakeEntity(getRandomInterest()));
            }
            for (int i = 0; i < random.nextInt(3); i++) {
                result.addCategory(buildFakeCategory(getRandomInterest()));
            }
        } catch (Exception e) {
            throw new NLPEngineException("Something went wrong in the MockEngine!", e);
        }
        return result;
    }

    @Override
    public NLPEngineResult enrich(URL url) throws NLPEngineException {
        NLPEngineResult result = new NLPEngineResult();
        try {
            if(url.equals(new URL("http://www.bbc.co.uk/news/uk-18494541"))) {
                result.addEntity(Entity.build("BBC", "BBC"));
            }
        } catch (Exception e) {
            throw new NLPEngineException("Something went wrong in the MockEngine!",e);
        }
        return result;
    }

    private String getRandomInterest() {
        String interest;
        int i = random.nextInt(15)+1;
        switch (i) {
            case 1 : interest = "BBC";
                break;
            case 2 : interest = "Doctor_Who";
                break;
            case 3 : interest = "London";
                break;
            case 4 : interest = "Sport";
                break;
            case 5 : interest = "Tennis";
                break;
            case 6 : interest = "Euro2012";
                break;
            case 7 : interest = "Semantic_Web";
                break;
            case 8 : interest = "Football";
                break;
            case 9 : interest = "Music";
                break;
            case 10 : interest = "Guitar";
                break;
            case 11 : interest = "Cars";
                break;
            case 12 : interest = "Swimming";
                break;
            case 13 : interest = "Java";
                break;
            case 14 : interest = "Apple";
                break;
            case 15 : interest = "Muse";
                break;
            default: interest = "BBC";
        }
        return interest;
    }

    private Entity buildFakeEntity(String interest) {
        return Entity.build(interest, interest);
    }

    private Category buildFakeCategory(String interest) {
        Category category = Category.build(interest, interest);
        category.setScore(random.nextDouble()*20);
        return category;
    }
}