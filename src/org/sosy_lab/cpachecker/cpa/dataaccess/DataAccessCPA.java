package org.sosy_lab.cpachecker.cpa.dataaccess;

import de.uni_freiburg.informatik.ultimate.smtinterpol.Config;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.core.defaults.AbstractCPA;
import org.sosy_lab.cpachecker.core.defaults.AutomaticCPAFactory;
import org.sosy_lab.cpachecker.core.interfaces.*;
import org.sosy_lab.cpachecker.cpa.cintp.CIntpCPAStatistics;
import org.sosy_lab.cpachecker.cpa.cintp.CIntpStatistics;

import javax.security.auth.login.AppConfigurationEntry;
import org.sosy_lab.common.configuration.Configuration;
import java.util.Collection;


public class DataAccessCPA extends AbstractCPA implements ConfigurableProgramAnalysis, StatisticsProvider {

    public static RaceNum raceNum;
    private Statistics stats;
    private DataAccessTransferRelation transfer;

    public static CPAFactory factory() {
        return AutomaticCPAFactory.forType(DataAccessCPA.class);
    }

    public DataAccessCPA(Configuration pConfig) throws InvalidConfigurationException {
        super("sep", "sep", new DataAccessTransferRelation(pConfig, new DataAccessStatistics()));

        // Statistic for time
        transfer = (DataAccessTransferRelation) super.getTransferRelation();
        stats = transfer.getStatistics();
        // Statistic for race
        raceNum = new RaceNum();
    }

    @Override
    public AbstractState getInitialState(CFANode node, StateSpacePartition partition) throws InterruptedException {
        return DataAccessState.getInitialInstance();
    }

    @Override
    public void collectStatistics(Collection<Statistics> statsCollection) {
        statsCollection.add(stats);
    }
}
