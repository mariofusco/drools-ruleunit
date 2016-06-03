/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.ruleunit;

import org.kie.api.runtime.KieSession;

import java.util.Arrays;
import java.util.stream.Stream;

public class UnitAwareKieSession {
    private final KieSession ksession;

    private final DataSource data = new DataSource();

    public UnitAwareKieSession( KieSession ksession ) {
        this.ksession = ksession;
        ksession.setGlobal( "data", data );
    }

    public <T extends RuleUnit> T exec(Class<T> unitClass, Object... args) {
        String unitName = unitClass.getSimpleName();
        T unit = createUnit( unitClass, args );
        data.setCurrentRuleUnit( unit );

        // TODO should we use a filter instead of an agenda group?
        // ksession.fireAllRules( match -> ( (RuleImpl) match.getRule() ).getAgendaGroup().equals( unitName ) );

        ksession.getAgenda().getAgendaGroup( unitName ).setFocus();
        ksession.fireAllRules();

        return unit;
    }

    private <T extends RuleUnit> T createUnit(Class<T> unitClass, Object... args) {
        return Stream.of( unitClass.getConstructors() )
                     .filter( c -> c.getParameters().length == args.length )
                     .findFirst()
                     .map( c -> {
                         try {
                             return (T) c.newInstance( args );
                         } catch (Exception e) {
                             throw new RuntimeException( e );
                         }
                     } )
                     .orElseThrow( () -> new RuntimeException( "Cannot find constructor for " + unitClass +
                                                               " with args " + Arrays.toString( args ) ) );
    }
}
