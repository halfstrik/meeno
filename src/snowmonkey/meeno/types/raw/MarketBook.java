package snowmonkey.meeno.types.raw;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import snowmonkey.meeno.types.ImmutbleType;
import snowmonkey.meeno.types.MarketId;
import snowmonkey.meeno.types.SelectionId;

import java.time.ZonedDateTime;
import java.util.List;

public final class MarketBook extends ImmutbleType {
    public final MarketId marketId;
    public final Boolean isMarketDataDelayed;
    public final MarketStatus status;
    public final int betDelay;
    public final Boolean bspReconciled;
    public final Boolean complete;
    public final Boolean inplay;
    public final int numberOfWinners;
    public final int numberOfRunners;
    public final int numberOfActiveRunners;
    public final ZonedDateTime lastMatchTime;
    public final Double totalMatched;
    public final Double totalAvailable;
    public final Boolean crossMatching;
    public final Boolean runnersVoidable;
    public final Long version;
    public final ImmutableList<Runner> runners;

    public MarketBook(MarketId marketId, Boolean isMarketDataDelayed, MarketStatus status, int betDelay, Boolean bspReconciled, Boolean complete, Boolean inplay, int numberOfWinners,
                      int numberOfRunners, int numberOfActiveRunners, ZonedDateTime lastMatchTime, Double totalMatched, Double totalAvailable, Boolean crossMatching,
                      Boolean runnersVoidable, Long version, List<Runner> runners) {
        this.marketId = marketId;
        this.isMarketDataDelayed = isMarketDataDelayed;
        this.status = status;
        this.betDelay = betDelay;
        this.bspReconciled = bspReconciled;
        this.complete = complete;
        this.inplay = inplay;
        this.numberOfWinners = numberOfWinners;
        this.numberOfRunners = numberOfRunners;
        this.numberOfActiveRunners = numberOfActiveRunners;
        this.lastMatchTime = lastMatchTime;
        this.totalMatched = totalMatched;
        this.totalAvailable = totalAvailable;
        this.crossMatching = crossMatching;
        this.runnersVoidable = runnersVoidable;
        this.version = version;
        this.runners = runners == null ? null : ImmutableList.copyOf(runners);
    }

    public ImmutableMap<SelectionId, Runner> runners() {
        ImmutableMap.Builder<SelectionId, Runner> builder = ImmutableMap.builder();
        if (runners != null)
            for (Runner runner : runners) {
                builder.put(runner.selectionId, runner);
            }
        return builder.build();
    }
}
