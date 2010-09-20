/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package pt.inevo.encontra.query.criteria.exps;

import pt.inevo.encontra.query.Query;

import java.io.Serializable;

/**
 * Parser for query languages that will be used by a {@link ExpressionQuery}.
 * A QueryParser is responsible for translating from some string-based query
 * language into {@link Expression}s. Parsers should be stateless.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public interface ExpressionParser
    extends Serializable {
//
//
//    /**
//     * Return a parsed intermediate form of the given query string.
//     */
//    public Object parse(String ql, Query query);
//
//    /**
//     * Use the parsed query form to set information such as candidate type,
//     * result type, etc that was encoded in the query string.
//     */
//    public void populate(Object parsed, Query query);
//
//    /**
//     * Parse the given query string.
//     */
//    public QueryExpressions eval(Object parsed, Query query, ExpressionFactory factory);
//
//    /**
//     * Parse the given value clauses.
//     */
//    public Value[] eval(String[] vals, Query query, ExpressionFactory factory);
}
