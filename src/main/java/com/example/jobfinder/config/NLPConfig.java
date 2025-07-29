//package com.example.jobfinder.config;
//
//
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Properties;
//
//@Configuration
//public class NLPConfig {
//
//    @Bean
//    public StanfordCoreNLP stanfordCoreNLP() {
//        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
//        props.setProperty("coref.algorithm", "statistical");
//        props.setProperty("tokenize.language", "en");
//        return new StanfordCoreNLP(props);
//    }
//}
