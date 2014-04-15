--<ScriptOptions statementTerminator="%"/>

SELECT * FROM PWMEVENT.biddata where daynum = 20120711 and agentid = 'concentrator1' and timestamp = '2012-07-11 11:17:36.926' %

CREATE VARIABLE price PWMEVENT.realArray %
CREATE VARIABLE demand PWMEVENT.realArray %
SET price = PWMEVENT.getPriceArray('Test', 'concentrator1', 'matcher', timestamp('2012-07-11 11:17:36.926')) %
SET demand = PWMEVENT.getDemandArray('Test', 'concentrator1', 'matcher', timestamp('2012-07-11 11:17:36.926')) %
SELECT t.index - 1 as step, t.price, t.demand from unnest(price, demand) WITH ORDINALITY AS t(price, demand, index) %
DROP VARIABLE price %
DROP VARIABLE demand %
