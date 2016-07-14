package com.cronutils.example;

import com.cronutils.builder.CronBuilder;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.parser.CronParser;

import java.util.Locale;

import static com.cronutils.model.CronType.QUARTZ;
import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

public class CronUtilsExamples {

    public static CronDefinition defineOwnCronDefinition() {
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

    public static CronDefinition getPredefinedCronDefinition(){
        return CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
    }

    public static CronParser createParserBasedOnDefinition(CronDefinition cronDefinition){
        return new CronParser(cronDefinition);
    }

    public static Cron obtainCronParsingStringExpression(CronParser parser, String cronExpression){
        return parser.parse(cronExpression);
    }

    public static Cron buildCron(){
        return CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ))
                .withYear(always())
                .withDoM(between(SpecialChar.L, 3))
                .withMonth(always())
                .withDoW(questionMark())
                .withHour(always())
                .withMinute(always())
                .withSecond(on(0))
                .instance();
    }

    public static String obtainStringExpressionFromCron(Cron cron){
        return cron.asString();
    }


    public static void main(String[] args) {
        System.out.println(obtainStringExpressionFromCron(buildCron()));
    }
}
