package rubit.coredata.audit

import org.springframework.data.domain.AuditorAware
import java.util.Optional

class DefaultAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> = Optional.empty()
}
