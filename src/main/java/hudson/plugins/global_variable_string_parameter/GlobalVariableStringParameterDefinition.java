/*
 * The MIT License
 *
 * Copyright (c) 2013, Patrick McKeown
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.global_variable_string_parameter;

import hudson.Extension;
import hudson.model.AutoCompletionCandidates;
import hudson.model.ParameterValue;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.util.FormValidation;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.*;
import java.util.Properties;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Scanner;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * String based parameter that supports substituting global variables.
 *
 * @author Patrick McKeown
 * @see {@link StringParameterDefinition}
 * @since 1.0
 */
public class GlobalVariableStringParameterDefinition extends StringParameterDefinition {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // search for variables of the form ${VARNAME} or $NoWhiteSpace
    private static final Pattern pattern = Pattern.compile("\\$\\{(.+)\\}|\\$(.+)\\s?");


    @DataBoundConstructor
    public GlobalVariableStringParameterDefinition(String name, String defaultValue, String description) {

        super(name, defaultValue, description);
    }

    private static void logMe(String log) {

        try {
            FileWriter myWriter = new FileWriter("/Users/glebpalchin/Desktop/filename.txt", true);
            myWriter.write(log + "\n");
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public ParameterValue createValue(String value) {
        StringParameterValue st = new StringParameterValue(super.getName(),
                replaceGlobalVars(value), super.getDescription());

        return st;
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        String trueValueBLEA = req.getRequestURIWithQueryString();

        trueValueBLEA = trueValueBLEA.split("/")[2];

        jo.put("value", trueValueBLEA);
        return (StringParameterValue) req.bindJSON(StringParameterValue.class,
                jo);
    }

    @Override
    public StringParameterValue getDefaultParameterValue() {
        return new StringParameterValue(super.getName(),
                replaceGlobalVars(super.getDefaultValue()),
                super.getDescription());
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {

        @Override
        public String getDisplayName() {
            return "Global Variable String Parameter";
        }

        public FormValidation doCheckGlobalName(@QueryParameter String value) throws IOException, ServletException {
            return FormValidation.ok();
        }

        public AutoCompletionCandidates doAutoCompleteGlobalName(@QueryParameter String value) {
            AutoCompletionCandidates candidates = new AutoCompletionCandidates();
            Set<String> propNames = GlobalNodeProperties.getProperties().keySet();
            for (String name : propNames) {

                // Autocomplete global variables with or without the ${} special characters
                if (name.startsWith(value.replaceAll("\\$|\\{|\\}", "")) || name.startsWith(value)) {
                    candidates.add("${" + name + "}");
                }
            }

            return candidates;
        }

        private static void logMe(String log) {
            try {
                FileWriter myWriter = new FileWriter("/Users/glebpalchin/Desktop/filename.txt", true);
                myWriter.write(log + "\n");
                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }

    public static boolean globalVarExists(String str) {
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            // If ${VARNAME} match found, return that group, else return $NoWhiteSpace group
            String globalVariable = (m.group(1) != null) ? m.group(1) : m.group(2);
            String globalValue = GlobalNodeProperties.getValue(globalVariable);
            if (globalValue != null) {
                return true;
            }
        }
        return false;
    }

    public static String replaceGlobalVars(String str) {
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            // If ${VARNAME} match found, return that group, else return $NoWhiteSpace group
            String globalVariable = (m.group(1) != null) ? m.group(1) : m.group(2);
            String globalValue = GlobalNodeProperties.getValue(globalVariable);
            if (globalValue != null) {
                //Replace the full match (group 0) to remove any $ and {}
                str = str.replace(m.group(0), globalValue);
            }
        }

        return str;
    }
}
