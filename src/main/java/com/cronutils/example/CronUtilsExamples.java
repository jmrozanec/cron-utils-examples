package com.cronutils.example;

import com.cronutils.builder.CronBuilder;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import java.util.Locale;

import static com.cronutils.model.CronType.QUARTZ;
import static com.cronutils.model.CronType.UNIX;
import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

/**
 * This class provides a main method to showcase common use cases for the cron-utils library.
 */
public class CronUtilsExamples {

    public static void main(String[] args) {
        /* Welcome to our cron-utils usage examples.
         * In the following lines, we will introduce you to main cron-utils features
         * so that you can start using them asap :)
         */

        //How do we instantiate cron definitions? Check the method below, to have a look at possible alternatives,
        howToWithCronDefinitions();

        //How do we parse cron expressions? Yes :) Can we even deal with multi-crons? Sure!
        howToWithCronParser();

        //we can programatically create Cron objects or even migrate cron expressions between formats :)
        howToBuildAndMigrateCronExpressions();

        /* ok, up to now we just worked on cron domain. But what about execution times?
         * We have a scheduling service, and would like to display previous/next execution
         * or time from last or to next execution. Can cron-utils help us with that? Sure!
         */
        howToWithExecutionTimes();

        /* To finish this demo, we would like to showcase another useful feature: human readable descriptions
         * Cron expressions are cryptic to most non-tech users: how can we bring some transparency to them?
         * We add the cron-utils dependency to the project, and ask for a human readable description!
         */
        howToHumanReadableDescriptions();

        //That's all! Join us on Gitter and send us feedback on how are you doing with cron-utils!
    }


    private static CronDefinition defineOwnCronDefinition() {
        //define your own cron: arbitrary fields are allowed and last field can be optional
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth()
                .supportsHash().supportsL().supportsW().and()
                .withMonth().and()
                .withDayOfWeek()
                .withIntMapping(7, 0) //we support non-standard non-zero-based numbers!
                .supportsHash().supportsL().supportsW().and()
                .withYear().optional().and()
                .instance();
    }

    /**
     * This method will walk you through how to create cron definitions:
     * by using pre-defined ones or building your own.
     */
    private static void howToWithCronDefinitions() {
        // Define your own cron: arbitrary fields are allowed and last field can be optional
        CronDefinition cronDefinition1 =
                CronDefinitionBuilder.defineCron()
                        .withSeconds().and()
                        .withMinutes().and()
                        .withHours().and()
                        .withDayOfMonth().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                        .withMonth().and()
                        .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsQuestionMark().and()
                        .withYear().withValidRange(1970, 2099).optional().and()
                        .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
                        .instance();

        // or get a predefined instance
        CronDefinition cronDefinition2 = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);

        System.out.println(String.format("Are those definitions the same? %s", cronDefinition1.equals(cronDefinition2)));
        //now you can use it to build a parser or to programatically build cron expressions!
    }

    /**
     * This method will walk you through how to parse cron expressions.
     */
    private static void howToWithCronParser() {
        //once we created a cron definition, we can use it to build a parser
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        String expression = "0 23 * ? * 1-5 *";
        Cron quartzCron = parser.parse(expression);

        System.out.println(String.format("Was the parsing correct? Lets turn it to string again! original: '%s' parsed: '%s'", expression, quartzCron.asString()));

        /* We can even parse multi-crons!
         * How about squashing multiple crons into a single line?
         * Instead of writting "0 0 9 * * ? *", "0 0 10 * * ? *", "0 30 11 * * ? *" and "0 0 12 * * ? *"
         * we can wrap it into "0 0|0|30|0 9|10|11|12 * * ? *"
         */
        String multicron = "0 0|0|30|0 9|10|11|12 * * ? *";
        Cron cron = parser.parse(multicron);
        System.out.println(String.format("Are those the same? %s", multicron.equals(cron.asString())));
    }

    /**
     * This method will walk you through how to programatically build cron expressions
     */
    private static void howToBuildAndMigrateCronExpressions() {
        //we can define crons programmatically
        Cron unixBuiltCronExpression = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(UNIX))
                .withDoM(between(1, 3))
                .withMonth(always())
                .withDoW(always())
                .withHour(always())
                .withMinute(always())
                .instance();
        String unixBuiltCronExpressionString = unixBuiltCronExpression.asString();

        Cron quartzBuiltCronExpression = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ))
                .withYear(always())
                .withDoM(between(1, 3))
                .withMonth(always())
                .withDoW(questionMark())
                .withHour(always())
                .withMinute(always())
                .withSecond(on(0))
                .instance();
        String quartzBuiltCronExpressionString = quartzBuiltCronExpression.asString();
        
        /* Definitions seem coupled to a specific representation ...
         * To avoid that, we can migrate anytime using a CronMapper :)
         * Let's see how we do that! Below we provide some scenarios, where this can be useful.
         */

        //What if we are migrating between cron formats?
        //Ex.: we had everything in Linux, and now we provide a Quartz based scheduling service with a nice REST API.
        //How can we perform the conversions, to remain equivalent while minimizing work?
        //cron-utils provides a CronMapper for that: we can also migrate from/to any other cron format
        String fromQuartzToUnixString = CronMapper.fromQuartzToUnix().map(quartzBuiltCronExpression).asString();
        String fromUnixToQuartzString = CronMapper.fromUnixToQuartz().map(unixBuiltCronExpression).asString();

        //Lets compare this expressions
        System.out.println(
                String.format("Original Quartz cron expression: '%s' Mapped from Unix: '%s'",
                        quartzBuiltCronExpressionString,
                        fromUnixToQuartzString
                )
        );
        System.out.println(
                String.format("Original Unix cron expression: '%s' Mapped from Quartz: '%s'",
                        unixBuiltCronExpressionString,
                        fromQuartzToUnixString
                )
        );

        //If we are not performing a migration, but just need to check if expressions are equivalent ...
        System.out.println(
                String.format("Are both expressions (Quartz: '%s' vs. Unix: '%s') equivalent? %s",
                        quartzBuiltCronExpressionString, unixBuiltCronExpressionString,
                        quartzBuiltCronExpression.equivalent(CronMapper.fromUnixToQuartz(), unixBuiltCronExpression)
                )
        );
    }

    private static void howToWithExecutionTimes(){
        //Given a Cron instance, we can ask for next/previous execution
        /*
        ZonedDateTime now = ZonedDateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(parsedQuartzCronExpression);
        System.out.println(
                String.format(
                        "Given the Quartz cron '%s' and reference date '%s', last execution was '%s'",
                        parsedQuartzCronExpression.asString(), now, executionTime.lastExecution(now)
                )
        );
        System.out.println(
                String.format(
                        "Given the Quartz cron '%s' and reference date '%s', next execution will be '%s'",
                        parsedQuartzCronExpression.asString(), now, executionTime.nextExecution(now)
                )
        );
        //or request time from last / to next execution
        Optional<Duration> timeFromLastExecution = executionTime.timeFromLastExecution(now);
        Optional<Duration> timeToNextExecution = executionTime.timeToNextExecution(now);
        System.out.println(
                String.format(
                        "Given the Quartz cron '%s' and reference date '%s', last execution was %s seconds ago",
                        parsedQuartzCronExpression.asString(), now, timeFromLastExecution.get().getSeconds()
                )
        );
        System.out.println(
                String.format(
                        "Given the Quartz cron '%s' and reference date '%s', next execution will be in %s seconds",
                        parsedQuartzCronExpression.asString(), now, timeToNextExecution.get().getSeconds()
                )
        );
        */
    }

    private static void howToHumanReadableDescriptions(){
        //we first need to setup a parser
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        String expression = "0 23 * ? * 1-5 *";

        //and then just ask for a description
        CronDescriptor descriptor = CronDescriptor.instance(Locale.US);//we support multiple languages! Just pick one!
        String quartzBuiltCronExpressionDescription = descriptor.describe(parser.parse(expression));
        System.out.println(
                String.format("Quartz expression '%s' is described as '%s'", expression, quartzBuiltCronExpressionDescription)
        );
    }
}
