package com.cronutils.example;

import com.cronutils.builder.CronBuilder;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Locale;

import static com.cronutils.model.CronType.QUARTZ;
import static com.cronutils.model.CronType.UNIX;
import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

public class CronUtilsExamples {

    public static void main(String[] args) {
        //Using CronBuilder we can build cron expressions for any format.
        Cron quartzBuiltCronExpression = buildQuartzCronExpressionUsingCronBuilder();
        Cron unixBuiltCronExpression = buildUnixCronExpressionUsingCronBuilder();

        //Once an expression is represented as Cron object, we can get a cron string
        String quartzBuiltCronExpressionString = quartzBuiltCronExpression.asString();
        String unixBuiltCronExpressionString = unixBuiltCronExpression.asString();

        //We can also migrate to any other cron format
        String fromQuartzToUnixString = CronMapper.fromQuartzToUnix().map(quartzBuiltCronExpression).asString();
        String fromUnixToQuartzString = CronMapper.fromUnixToQuartz().map(unixBuiltCronExpression).asString();

        //We can now compare this expressions
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

        //Given the expressions are in different formats, are they equivalent?
        System.out.println(
                String.format("Are both expressions (Quartz: '%s' vs. Unix: '%s') equivalent? %s",
                        quartzBuiltCronExpressionString, unixBuiltCronExpressionString,
                        quartzBuiltCronExpression.equivalent(CronMapper.fromUnixToQuartz(), unixBuiltCronExpression)
                )
        );

        //We can also get a Cron instance parsing some String cron expression
        CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        Cron parsedQuartzCronExpression = quartzCronParser.parse(quartzBuiltCronExpressionString);

        //we can validate expression is valid
        System.out.println(
                String.format("If the following expression fails to validate, we get an exception! Validated expression is: '%s'",
                        parsedQuartzCronExpression.validate().asString()
                )
        );

        //In any case, given a Cron instance, we can ask for next/previous execution
        DateTime now = DateTime.now();
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
        //we can also request time from last / to next execution
        Duration timeFromLastExecution = executionTime.timeFromLastExecution(now);
        Duration timeToNextExecution = executionTime.timeToNextExecution(now);
        System.out.println(
                String.format(
                        "Given the Quartz cron '%s' and reference date '%s', last execution was %s seconds ago",
                        parsedQuartzCronExpression.asString(), now, timeFromLastExecution.toStandardSeconds().getSeconds()
                )
        );
        System.out.println(
                String.format(
                        "Given the Quartz cron '%s' and reference date '%s', next execution will be in %s seconds",
                        parsedQuartzCronExpression.asString(), now, timeToNextExecution.toStandardSeconds().getSeconds()
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
