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

import org.drools.core.common.InternalAgendaGroup;
import org.drools.ruleunit.reactive.ReactiveCollection;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class UnitAwareKieSession {
    private final KieSession ksession;

    public UnitAwareKieSession( KieSession ksession ) {
        this.ksession = ksession;
    }

    public <T extends RuleUnit> T exec(Class<T> unitClass, Object... args) {
        T unit = setupUnit( unitClass, args );
        ksession.fireAllRules();
        return unit;
    }

    public <T extends RuleUnit> T execUntilHalt(Class<T> unitClass, Object... args) {
        T unit = setupUnit( unitClass, args );
        new Thread( () -> ksession.fireUntilHalt() ).start();
        return unit;
    }

    private <T extends RuleUnit> T setupUnit( Class<T> unitClass, Object[] args ) {
        String unitName = unitClass.getSimpleName();
        T unit = createUnit( unitClass, args );
        bindData( unit );
        InternalAgendaGroup agendaGroup = (InternalAgendaGroup) ksession.getAgenda().getAgendaGroup( unitName );
        agendaGroup.setAutoDeactivate( false );
        agendaGroup.setFocus();
        return unit;
    }

    public void halt() {
        ksession.halt();
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

    private void bindData( RuleUnit unit ) {
        Stream.of( unit.getClass().getFields() )
              .map( f -> new EntryPointDataSource(unit, ksession.getEntryPoint( f.getName() ), f ) )
              .filter( EntryPointDataSource::isValid )
              .forEach( EntryPointDataSource::bindData );
    }

    private static class EntryPointDataSource {
        private final RuleUnit unit;
        private final EntryPoint ep;
        private final Field field;

        private EntryPointDataSource( RuleUnit unit, EntryPoint ep, Field field ) {
            this.unit = unit;
            this.ep = ep;
            this.field = field;
        }

        public boolean isValid() {
            return ep != null;
        }

        public void bindData() {
            try {
                Object data = field.get(unit);
                if (data instanceof Collection) {
                    ( (Collection) data ).stream().forEach( ep::insert );
                    if (data instanceof ReactiveCollection) {
                        ( (ReactiveCollection) data ).register( ep::insert );
                    }
                } else {
                    ep.insert( data );
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException( e );
            }
        }
    }
}
