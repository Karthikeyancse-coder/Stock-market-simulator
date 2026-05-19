-- ============================================================
-- StockSim India — Seed Data
-- Uses INSERT IGNORE for idempotent seeding
-- ============================================================

-- Market Config
INSERT IGNORE INTO market_config (id, is_market_open, update_interval_sec, candle_interval_sec)
VALUES (1, true, 5, 60);

-- ============================================================
-- 20 NSE Stocks
-- ============================================================
INSERT IGNORE INTO stocks (symbol, company_name, sector, current_price, prev_close, open_price, high_today, low_today, volume, volatility, is_active, updated_at)
VALUES
('RELIANCE',   'Reliance Industries Ltd',      'Energy',         2850.00, 2820.00, 2825.00, 2870.00, 2810.00,  8500000, 0.0180, true, NOW()),
('TCS',        'Tata Consultancy Services',    'IT',             3920.00, 3890.00, 3895.00, 3940.00, 3880.00,  3200000, 0.0140, true, NOW()),
('INFOSYS',    'Infosys Ltd',                  'IT',             1485.00, 1470.00, 1472.00, 1495.00, 1465.00,  5800000, 0.0160, true, NOW()),
('HDFCBANK',   'HDFC Bank Ltd',                'Banking',        1620.00, 1605.00, 1608.00, 1635.00, 1598.00,  7100000, 0.0130, true, NOW()),
('ICICIBANK',  'ICICI Bank Ltd',               'Banking',        1085.00, 1070.00, 1072.00, 1092.00, 1065.00,  9200000, 0.0150, true, NOW()),
('WIPRO',      'Wipro Ltd',                    'IT',              465.00,  460.00,  461.00,  470.00,  457.00,  4100000, 0.0190, true, NOW()),
('BAJFINANCE', 'Bajaj Finance Ltd',            'Finance',        6850.00, 6780.00, 6800.00, 6900.00, 6750.00,  1800000, 0.0220, true, NOW()),
('TATAMOTORS', 'Tata Motors Ltd',              'Automobile',      985.00,  970.00,  972.00,  995.00,  965.00, 12000000, 0.0250, true, NOW()),
('HINDUNILVR', 'Hindustan Unilever Ltd',       'FMCG',           2380.00, 2360.00, 2365.00, 2395.00, 2350.00,  2100000, 0.0110, true, NOW()),
('MARUTI',     'Maruti Suzuki India Ltd',      'Automobile',    11200.00,11050.00,11080.00,11280.00,11020.00,   850000, 0.0170, true, NOW()),
('ASIANPAINT', 'Asian Paints Ltd',             'Paints',         2890.00, 2860.00, 2865.00, 2910.00, 2848.00,  1500000, 0.0160, true, NOW()),
('ITC',        'ITC Ltd',                      'FMCG',            465.00,  460.00,  462.00,  470.00,  458.00, 18000000, 0.0130, true, NOW()),
('KOTAKBANK',  'Kotak Mahindra Bank',          'Banking',        1780.00, 1760.00, 1765.00, 1795.00, 1752.00,  3800000, 0.0150, true, NOW()),
('LT',         'Larsen & Toubro Ltd',          'Infrastructure', 3560.00, 3520.00, 3530.00, 3580.00, 3510.00,  2200000, 0.0180, true, NOW()),
('SBIN',       'State Bank of India',          'Banking',         785.00,  775.00,  778.00,  792.00,  771.00, 22000000, 0.0200, true, NOW()),
('ONGC',       'Oil & Natural Gas Corp',       'Energy',          268.00,  264.00,  265.00,  272.00,  262.00, 15000000, 0.0210, true, NOW()),
('COALINDIA',  'Coal India Ltd',               'Mining',          458.00,  452.00,  454.00,  463.00,  449.00,  8500000, 0.0180, true, NOW()),
('TITAN',      'Titan Company Ltd',            'Consumer',       3680.00, 3640.00, 3648.00, 3700.00, 3628.00,  1200000, 0.0190, true, NOW()),
('NESTLEIND',  'Nestle India Ltd',             'FMCG',          24500.00,24200.00,24280.00,24650.00,24150.00,   180000, 0.0120, true, NOW()),
('ADANIPORTS', 'Adani Ports & SEZ',            'Infrastructure', 1320.00, 1295.00, 1300.00, 1335.00, 1288.00,  5500000, 0.0240, true, NOW());

-- ============================================================
-- 10 Badges
-- ============================================================
INSERT INTO badges (code, name, description, icon, progress_type, progress_target) VALUES
('FIRST_TRADE',   'First Trade',      'Complete your very first buy order',                              '🎯', 'TRADE_COUNT',          1),
('BIG_SPENDER',   'Big Spender',      'Make a single trade worth ₹25,000 or more',                       '💸', 'SINGLE_TRADE_AMOUNT', 25000),
('QUICK_SELLER',  'Quick Seller',     'Sell a stock within 60 seconds of buying it',                     '⚡', 'TIME_BASED',           60),
('IN_THE_GREEN',  'In The Green',     'Achieve 5% profit on any single holding',                         '📈', 'PROFIT_PERCENT',        5),
('DIVERSIFIED',   'Diversified',      'Hold 5 different stocks simultaneously',                          '🔀', 'UNIQUE_STOCKS',         5),
('DIAMOND_HANDS', 'Diamond Hands',    'Hold any stock for 7 or more simulated days',                     '💎', 'HOLD_DAYS',             7),
('COMEBACK_KID',  'Comeback Kid',     'Recover portfolio value after a 10% dip below starting capital',  '🚀', 'RECOVERY',             10),
('HALF_WAY',      'Half Way There',   'Portfolio total value reaches ₹1,50,000',                         '🎯', 'PORTFOLIO_VALUE',   150000),
('DOUBLE_UP',     'Double Up',        'Portfolio total value reaches ₹2,00,000',                         '🤑', 'PORTFOLIO_VALUE',   200000),
('VETERAN',       'Market Veteran',   'Complete 25 total trades',                                        '🎖️', 'TRADE_COUNT',          25)
ON DUPLICATE KEY UPDATE 
  name = VALUES(name), description = VALUES(description), icon = VALUES(icon), 
  progress_type = VALUES(progress_type), progress_target = VALUES(progress_target);

-- ============================================================
-- Historical OHLC candles (100 per stock, going back 100 minutes)
-- ============================================================

-- RELIANCE historical OHLC
INSERT IGNORE INTO ohlc_data (stock_id, open_price, high_price, low_price, close_price, volume, candle_time)
SELECT s.id,
  ROUND(2850.00 * (1 + (RAND()-0.5)*0.02), 2) AS open_price,
  ROUND(2850.00 * (1 + RAND()*0.01 + 0.005), 2) AS high_price,
  ROUND(2850.00 * (1 - RAND()*0.01 - 0.005), 2) AS low_price,
  ROUND(2850.00 * (1 + (RAND()-0.5)*0.02), 2) AS close_price,
  FLOOR(50000 + RAND()*100000) AS volume,
  DATE_SUB(NOW(), INTERVAL (n.n) MINUTE) AS candle_time
FROM stocks s
CROSS JOIN (
  SELECT a.N + b.N * 10 + 1 AS n
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
       (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
  ORDER BY n
) n
WHERE s.symbol = 'RELIANCE';

INSERT IGNORE INTO ohlc_data (stock_id, open_price, high_price, low_price, close_price, volume, candle_time)
SELECT s.id,
  ROUND(3920.00 * (1 + (RAND()-0.5)*0.015), 2),
  ROUND(3920.00 * (1 + RAND()*0.01 + 0.004), 2),
  ROUND(3920.00 * (1 - RAND()*0.01 - 0.004), 2),
  ROUND(3920.00 * (1 + (RAND()-0.5)*0.015), 2),
  FLOOR(20000 + RAND()*60000),
  DATE_SUB(NOW(), INTERVAL (n.n) MINUTE)
FROM stocks s
CROSS JOIN (
  SELECT a.N + b.N * 10 + 1 AS n
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
       (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
  ORDER BY n
) n
WHERE s.symbol = 'TCS';

INSERT IGNORE INTO ohlc_data (stock_id, open_price, high_price, low_price, close_price, volume, candle_time)
SELECT s.id,
  ROUND(1485.00 * (1 + (RAND()-0.5)*0.016), 2),
  ROUND(1485.00 * (1 + RAND()*0.01 + 0.004), 2),
  ROUND(1485.00 * (1 - RAND()*0.01 - 0.004), 2),
  ROUND(1485.00 * (1 + (RAND()-0.5)*0.016), 2),
  FLOOR(30000 + RAND()*80000),
  DATE_SUB(NOW(), INTERVAL (n.n) MINUTE)
FROM stocks s
CROSS JOIN (
  SELECT a.N + b.N * 10 + 1 AS n
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
       (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
  ORDER BY n
) n
WHERE s.symbol = 'INFOSYS';

INSERT IGNORE INTO ohlc_data (stock_id, open_price, high_price, low_price, close_price, volume, candle_time)
SELECT s.id,
  ROUND(1620.00 * (1 + (RAND()-0.5)*0.013), 2),
  ROUND(1620.00 * (1 + RAND()*0.008 + 0.003), 2),
  ROUND(1620.00 * (1 - RAND()*0.008 - 0.003), 2),
  ROUND(1620.00 * (1 + (RAND()-0.5)*0.013), 2),
  FLOOR(40000 + RAND()*90000),
  DATE_SUB(NOW(), INTERVAL (n.n) MINUTE)
FROM stocks s
CROSS JOIN (
  SELECT a.N + b.N * 10 + 1 AS n
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
       (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
  ORDER BY n
) n
WHERE s.symbol = 'HDFCBANK';

INSERT IGNORE INTO ohlc_data (stock_id, open_price, high_price, low_price, close_price, volume, candle_time)
SELECT s.id,
  ROUND(1085.00 * (1 + (RAND()-0.5)*0.015), 2),
  ROUND(1085.00 * (1 + RAND()*0.009 + 0.003), 2),
  ROUND(1085.00 * (1 - RAND()*0.009 - 0.003), 2),
  ROUND(1085.00 * (1 + (RAND()-0.5)*0.015), 2),
  FLOOR(50000 + RAND()*100000),
  DATE_SUB(NOW(), INTERVAL (n.n) MINUTE)
FROM stocks s
CROSS JOIN (
  SELECT a.N + b.N * 10 + 1 AS n
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
       (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
  ORDER BY n
) n
WHERE s.symbol = 'ICICIBANK';

INSERT IGNORE INTO ohlc_data (stock_id, open_price, high_price, low_price, close_price, volume, candle_time)
SELECT s.id,
  ROUND(s.current_price * (1 + (RAND()-0.5)*s.volatility), 2),
  ROUND(s.current_price * (1 + RAND()*s.volatility*0.5 + 0.003), 2),
  ROUND(s.current_price * (1 - RAND()*s.volatility*0.5 - 0.003), 2),
  ROUND(s.current_price * (1 + (RAND()-0.5)*s.volatility), 2),
  FLOOR(10000 + RAND()*50000),
  DATE_SUB(NOW(), INTERVAL (n.n) MINUTE)
FROM stocks s
CROSS JOIN (
  SELECT a.N + b.N * 10 + 1 AS n
  FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
       (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
  ORDER BY n
) n
WHERE s.symbol IN ('WIPRO','BAJFINANCE','TATAMOTORS','HINDUNILVR','MARUTI',
                   'ASIANPAINT','ITC','KOTAKBANK','LT','SBIN',
                   'ONGC','COALINDIA','TITAN','NESTLEIND','ADANIPORTS');
