package org.jenkinsci.plugins.bart;

import com.github.tzemp.config.Config;
import com.github.tzemp.parser.Parser;
import com.github.tzemp.parser.ParserSummary;
import com.github.tzemp.parser.StackExchangeQuestionEvaluation;
import com.github.tzemp.reporting.ReportingRequest;
import com.github.tzemp.stackoverflow.StackExchangeQuestion;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by timothyzemp on 29.09.17.
 */
public class LogParserBuildAction implements Action {

    private String message;
    private AbstractBuild<?, ?> build;
    private ParserSummary parserSummary;
    private List<StackExchangeQuestionEvaluation> evaluations;

    @Override
    public String getIconFileName() {
        return "star-gold.png";
    }

    @Override
    public String getDisplayName() {
        return "BART";
    }

    @Override
    public String getUrlName() {
        return "bart";
    }

    public String getMessage() {
        return this.message;
    }

    public List<StackExchangeQuestionEvaluation> getEvaluations() { return this.evaluations; }

    public ParserSummary getParserSummary() { return this.parserSummary; }

    public int getBuildNumber() {
        return this.build.number;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    LogParserBuildAction(final String message, final AbstractBuild<?, ?> build) throws IOException {
        System.out.print("START PARSER");
        this.build = build;
        Config config = this.readConfig();
        Parser parser = new Parser(build.getLog(Integer.MAX_VALUE), config);
        this.message = StringUtils.join(parser.print().toArray(), '\n');
        this.parserSummary = parser.getParserSummary();
        this.evaluations = parser.getEvaluations();

        if (false) {
            System.out.print("SEND ANALYTICS");
            ReportingRequest request = new ReportingRequest();
            request.post(this.parserSummary, parser.getLog(), "testproject");
            System.out.print("END SEND ANALYTICS");

        }

    }

    private Config readConfig() {
        System.out.print("START READING CONFIG");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Config config = mapper.readValue(new File("src/config/config.yaml"), Config.class);
            System.out.print("END READING CONFIG - SUCCESS");
            return config;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.print("END READING CONFIG - FAILURE");
        return null;
    }
}
