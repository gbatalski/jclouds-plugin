/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.chef;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import nl.javadude.scannit.Configuration;
import nl.javadude.scannit.Scannit;
import nl.javadude.scannit.scanner.TypeAnnotationScanner;

import org.jclouds.scriptbuilder.domain.Statement;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;


import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.ChefRecipeExecutorFunction.RunList;
import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution.CassandraChefExecutorStrategy;
import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution.CassandraExecutorSupplier;
import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution.Strategy;
import edu.kit.aifb.gb.utils.FilterIntegers;

/**
 * @author gena
 *
 */

public class ChefRecipeExecutorFunctionTest {

    /**
     * Test method for {@link edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.ChefRecipeExecutorFunction.RunList#RunList(java.lang.Iterable)}.
     */
    @Test
    public void testRunList() {
	RunList rl = getSample();
	assertNotNull(rl);
	assertTrue(copyOf(rl.getRecipes()).size() == 2);
    }

    /**
     * Test method for {@link edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.ChefRecipeExecutorFunction.RunList#toJSON()}.
     */
    @Test
    public void testToJSON() {
	String json = getSample().toJSON();
	assertNotNull(emptyToNull(json));
    }

    /**
     * Test method for {@link edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.ChefRecipeExecutorFunction.RunList#fromJSON(java.lang.String)}.
     */
    @Test
    public void testFromJSON() {
	RunList rl = RunList.fromJSON(
		"// sample json \n" +
		"{"+
		   "\"cassandra\":{"+
		   "\"java_heap_size_min\" :  \"32M\","+
		   "\"java_heap_size_max\" : \"128M\","+
		   "\"java_heap_size_eden\" : \"32M\","+
		   "\"home_dir\" : \"/usr/share/cassandra\""+
		   "},"+
		   "\"run_list\": ["+
		   "\"recipe[cassandra::install_from_package]\","+
		    "\"recipe[cassandra::mx4j]\","+
		    "\"recipe[cassandra::server]\""+
		  "]"+
		"}");
	assertNotNull(rl.getRecipes());
	assertTrue(copyOf(rl.getRecipes()).size() == 3);
    }

    /**
     * Test method for
     * {@link edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.ChefRecipeExecutorFunction.RunList#getRecipes()}
     * .
     */
    @Test
    public void testGetRunList() {
	RunList rl = getSample();
	assertNotNull(rl);
	assertTrue(copyOf(rl.getRecipes()).size() == 2);
    }

    /**
     * Test method for {@link edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.ChefRecipeExecutorFunction.RunList#getKnifeStatement()}.
     */
    @Test
    public void testGetKnifeStatement() {
	Statement statement = getSample().yieldKnifeStatement("/tmp/solo.rb", ImmutableList.<String> of());
	assertNotNull(statement);
    }

    private RunList getSample(){
	return new RunList(of("one", "two"));
    }

    @Test
    public void testInteger() {
	Iterable<Integer> numbers = FilterIntegers.filterIntegers(Splitter.onPattern("\\s|,|;").split("1 2 3 , test2 ; test 12"));
	assertTrue(copyOf(numbers).size() == 4);
    }

    @Test
    public void mapOps() {
	ImmutableMultimap.Builder<String, String> labeledIps = ImmutableMultimap.<String, String> builder();
	labeledIps.put("a", "10");
	labeledIps.put("a", "20");
	labeledIps.put("b", "10");
	labeledIps.put("b", "20");

	Map<String, String> m = Maps.transformValues(labeledIps.build().asMap(), new Function<Iterable<String>, String>() {

	    @Override
	    public String apply(Iterable<String> input) {

		return Joiner.on(",").join(input);
	    }

	});
	assertNotNull(m);
    }

    @Test
    public void testScanit() {
	Set<Class<?>> strategyClazzes;

	Configuration config = Configuration.config().with(new TypeAnnotationScanner())
		.scan("edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution");
	Scannit scannit = new Scannit(config);
	strategyClazzes = scannit.getTypesAnnotatedWith(Strategy.class);

    }

    @Test
    public void testTokenCreation() {
	System.out.println(Joiner.on("\n").join(CassandraExecutorSupplier.calcTokens(3)));
    }
}
