/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.xet.sparwings.spring.web.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import jp.xet.baseunits.timeutil.Clock;
import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RateLimitService} implementation to store values in memory.
 * 
 * @since 0.8
 * @author daisuke
 */
public class InMemoryRateLimitService extends AbstractRateLimitService {
	
	private static Logger logger = LoggerFactory.getLogger(InMemoryRateLimitService.class);
	
	private Map<String, RateLimitSpec> specs = new ConcurrentHashMap<>();
	
	
	@Override
	public synchronized RateLimitDescriptor consume(HttpServletRequest request, long consumption) {
		RateLimitRecovery recovery = computeRateLimitRecovery(request);
		if (recovery == null) {
			return null;
		}
		String limitationUnitName = recovery.getLimitationUnitName();
		long fillRate = recovery.getFillRate();
		long maxBudget = recovery.getMaxBudget();
		
		long now = Clock.now().toEpochSec();
		RateLimitSpec spec = specs.computeIfAbsent(limitationUnitName,
				p -> new RateLimitSpec(p, fillRate, maxBudget, maxBudget, now));
		long secSinceLastUpdate = now - spec.getLastUpdateTime();
		logger.info("Time (sec) since last update = {}", secSinceLastUpdate);
		long fill = secSinceLastUpdate * spec.getFillRate();
		long budget = Math.min(spec.getMaxBudget(), spec.getCurrentBudget() + fill) - consumption;
		spec.lastUpdateTime = now;
		
		logger.info("Current budget and consumption: (filled {}) and {} - {}", fill, budget + consumption, consumption);
		spec.setCurrentBudget(budget);
		
		return spec;
	}
	
	@Override
	public RateLimitDescriptor get(HttpServletRequest request) {
		RateLimitRecovery recovery = computeRateLimitRecovery(request);
		if (recovery == null) {
			return null;
		}
		String limitationUnitName = recovery.getLimitationUnitName();
		long fillRate = recovery.getFillRate();
		long maxBudget = recovery.getMaxBudget();
		
		long now = Clock.now().toEpochSec();
		RateLimitSpec spec = specs.computeIfAbsent(limitationUnitName,
				p -> new RateLimitSpec(limitationUnitName, fillRate, maxBudget, maxBudget, now));
		long secSinceLastUpdate = now - spec.getLastUpdateTime();
		logger.info("Time (sec) since last update = {}", secSinceLastUpdate);
		long fill = secSinceLastUpdate * spec.getFillRate();
		long budget = Math.min(spec.getMaxBudget(), spec.getCurrentBudget() + fill);
		spec.lastUpdateTime = now;
		
		spec.setCurrentBudget(budget);
		logger.info("Current budget: (filled {}) and {}", fill, spec.getCurrentBudget());
		return spec;
	}
	
	
	private static class RateLimitSpec extends RateLimitDescriptor {
		
		@Getter
		private long lastUpdateTime;
		
		
		public RateLimitSpec(String limitationUnit, long fillRate, long maxBudget, long currentBudget,
				long lastUpdateTime) {
			super(limitationUnit, fillRate, maxBudget, currentBudget);
			this.lastUpdateTime = lastUpdateTime;
		}
	}
}
