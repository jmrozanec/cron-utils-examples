package com.cronutils.example;

import com.cronutils.builder.CronBuilder;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Locale;

import static com.cronutils.model.CronType.QUARTZ;
import static com.cronutils.model.CronType.UNIX;
import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

/**
 * This class provides a main method to showcase common use cases for the cron-utils library.
 */
public class CronUtilsExamples {

    public static void main(String[] args) {
        //Welcome to our cron-utils usage examples.
        //In the following lines, we will outline the most important use cases you can leverage.

        //First we want to create a Cron object. This can be done by building it or parsing some expression.
        //Using CronBuilder we can build cron expressions for any format.
        Cron quartzBuiltCronExpression = buildQuartzCronExpressionUsingCronBuilder();
        Cron unixBuiltCronExpression = buildUnixCronExpressionUsingCronBuilder();
        //Otherwise CronParser will aid us on parsing some cron string expression into a Cron instance
        String quartzCronExpression = "0 * * 1-3 * ? *";
        CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        Cron parsedQuartzCronExpression = quartzCronParser.parse(quartzCronExpression);

        //Once an expression is represented as Cron object, we can get a cron string
        String quartzBuiltCronExpressionString = quartzBuiltCronExpression.asString();
        String unixBuiltCronExpressionString = unixBuiltCronExpression.asString();
        //We can compare if the string we obtained, is the same to the original one!
        System.out.println(
                String.format("Original expression: '%s', After parsing to Cron: '%s'. Are the same? %s",
                        quartzCronExpression,
                        parsedQuartzCronExpression.asString(),
                        quartzCronExpression.equals(parsedQuartzCronExpression.asString())
                )
        );

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

        //we can also check if a given expression is valid
        System.out.println(
                String.format("If the following expression fails to validate, we get an exception! Validated expression is: '%s'",
                        parsedQuartzCronExpression.validate().asString()
                )
        );

        //ok, up to now we just worked on cron domain. But what about execution times?
        //We have a scheduling service, and would like to display previous/next execution
        //or time from last or to next execution. Can cron-utils help us with that? Sure!
        //Given a Cron instance, we can ask for next/previous execution
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
        Duration timeFromLastExecution = executionTime.timeFromLastExecution(now);
        Duration timeToNextExecution = executionTime.timeToNextExecution(now);
        System.out.println(
                String.format(
                        "Given the Quartz cron '%s' and reference date '%s', last execution was %s seconds ago",
                        parsedQuartzCronExpression.asString(), now, timeFromLastExecution.getSeconds()
                )
        );
        System.out.println(
                String.format(
                        "Given the Quartz cron '%s' and reference date '%s', next execution will be in %s seconds",
                        parsedQuartzCronExpression.asString(), now, timeToNextExecution.getSeconds()
                )
        );

        //To finish this demo, we would like to showcase another useful feature: human readable descriptions
        //Cron expressions are cryptic to most non-tech users: how can we bring some transparency to them?
        //We add the cron-utils dependency to the project, and ask for a human readable description!
        CronDescriptor descriptor = CronDescriptor.instance(Locale.US);//we support multiple languages! Just pick one!
        String quartzBuiltCronExpressionDescription = descriptor.describe(quartzBuiltCronExpression);
        System.out.println(
                String.format("Quartz expression '%s' is described as '%s'", quartzBuiltCronExpression.asString(), quartzBuiltCronExpressionDescription)
        );

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
                .withYear().and()
                .lastFieldOptional()
                .instance();
    }

    private static Cron buildQuartzCronExpressionUsingCronBuilder(){
        //Create a cron expression. CronMigrator will ensure you remain cron provider agnostic
        return CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ))
                .withYear(always())
                .withDoM(between(1, 3))
                .withMonth(always())
                .withDoW(questionMark())
                .withHour(always())
                .withMinute(always())
                .withSecond(on(0))
                .instance();
    }

    private static Cron buildUnixCronExpressionUsingCronBuilder(){
        //Create a cron expression. CronMigrator will ensure you remain cron provider agnostic
        return CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(UNIX))
                .withDoM(between(1, 3))
                .withMonth(always())
                .withDoW(always())
                .withHour(always())
                .withMinute(always())
                .instance();
    }
}
